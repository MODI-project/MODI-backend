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

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 파일 업로드
    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

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

        boolean exists;
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFilename)
                    .build());
            exists = true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                exists = false;
            } else {
                throw new CustomException(S3ExceptionResponseStatus.S3_UPLOAD_VERIFY_FAILED);
            }
        }

        if (!exists) {
            throw new CustomException(S3ExceptionResponseStatus.S3_UPLOAD_VERIFY_FAILED);
        }

        return getFileUrl(uniqueFilename);
    }

    // 파일 URL 반환
    public String getFileUrl(String fileName) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
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