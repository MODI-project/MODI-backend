package kuit.modi.service;

import kuit.modi.exception.CustomException;
import kuit.modi.exception.S3ExceptionResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public record UploadResult(String key, String url) {}

    // 파일 업로드
    public UploadResult uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID() + "_" + (originalFilename == null ? "file" : originalFilename);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFilename)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new CustomException(S3ExceptionResponseStatus.S3_UPLOAD_FAILED);
        }

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFilename)
                    .build());
        } catch (S3Exception e) {
            throw new CustomException(S3ExceptionResponseStatus.S3_UPLOAD_VERIFY_FAILED);
        }
        String presignedUrl = getFileUrl(uniqueFilename);

        return new UploadResult(uniqueFilename, presignedUrl);
    }

    // 파일 URL 반환 (프리사인드 GET URL)
    public String getFileUrl(String fileName) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .getObjectRequest(getReq)
                .signatureDuration(Duration.ofMinutes(30))
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignReq);
        return presigned.url().toString();
    }

    public void deleteByKey(String key) {
        try {
            String normalizedKey = normalizeKey(key); // 앞의 "/" 제거 + URL 디코드
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(normalizedKey)
                    .build());
        } catch (Exception e) {
            throw new CustomException(S3ExceptionResponseStatus.S3_DELETE_FAILED);
        }
    }

    public void deleteFileFromUrl(String urlOrKey) {
        try {
            String key = resolveKey(urlOrKey);        // URL이면 key 추출, key면 그대로
            String normalizedKey = normalizeKey(key); // 앞의 "/" 제거 + URL 디코드
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(normalizedKey)
                    .build());
        } catch (Exception e) {
            throw new CustomException(S3ExceptionResponseStatus.S3_DELETE_FAILED);
        }
    }

    private String resolveKey(String urlOrKey) {
        // http(s)로 시작하면 URL로 간주하여 key 추출
        if (urlOrKey.startsWith("http://") || urlOrKey.startsWith("https://")) {
            try {
                URL u = new URL(urlOrKey);
                String host = u.getHost();       // 예: <bucket>.s3.ap-northeast-2.amazonaws.com 또는 CloudFront 도메인
                String path = u.getPath();       // 예: /<key> 또는 /<bucket>/<key>
                if (path == null) return "";

                // path 앞의 "/" 제거
                String p = path.startsWith("/") ? path.substring(1) : path;

                // 경로스타일: s3.<region>.amazonaws.com/<bucket>/<key>
                if (host != null && host.contains("amazonaws.com")) {
                    if (p.startsWith(bucketName + "/")) {
                        return p.substring(bucketName.length() + 1);
                    }
                    return p;
                }

                // 가상호스팅 또는 CloudFront: <bucket>.* 또는 커스텀 도메인 → path가 곧 key
                // (일부 구성에선 /<bucket>/<key>일 수 있으므로 한번 더 방어)
                if (p.startsWith(bucketName + "/")) {
                    return p.substring(bucketName.length() + 1);
                }
                return p;
            } catch (Exception e) {
                // URL 파싱 실패 시 원본을 key로 취급
                return urlOrKey;
            }
        }
        // 이미 key인 경우
        return urlOrKey;
    }

    private String normalizeKey(String key) {
        String k = (key.startsWith("/")) ? key.substring(1) : key;
        // 퍼센트 인코딩이 있는 경우에만 디코딩 ( '+' 오해방지 )
        if (k.indexOf('%') >= 0) {
            try {
                k = URLDecoder.decode(k, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ignore) {
                // 잘못된 인코딩이면 원본 유지
            }
        }
        return k;
    }
}