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
    private final S3Presigner s3Presigner; // ★ 추가

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

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
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception e) {
            throw new CustomException(S3ExceptionResponseStatus.S3_DELETE_FAILED);
        }
    }

    public void deleteFileFromUrl(String url) {
        try {
            URL s3Url = new URL(url);
            String rawKey = s3Url.getPath().substring(1); // "/abc.png" → "abc.png"
            String decodedKey = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);

            System.out.println("버킷 이름: " + bucketName);
            System.out.println("삭제 대상 URL: " + url);
            System.out.println("최종 삭제 대상 key = " + decodedKey);

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(decodedKey)
                    .build());
        } catch (Exception e) {
            throw new CustomException(S3ExceptionResponseStatus.S3_DELETE_FAILED);
        }
    }

    private String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}