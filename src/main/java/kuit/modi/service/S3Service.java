package kuit.modi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import kuit.modi.exception.CustomException;
import kuit.modi.exception.S3ExceptionResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 파일 업로드
    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, uniqueFilename, file.getInputStream(), metadata));
        } catch (IOException e) {
            throw new CustomException(S3ExceptionResponseStatus.S3_UPLOAD_FAILED);
        }

        boolean exists = amazonS3.doesObjectExist(bucketName, uniqueFilename);
        if (!exists) {
            throw new CustomException(S3ExceptionResponseStatus.S3_UPLOAD_VERIFY_FAILED);
        }

        return getFileUrl(uniqueFilename);
    }

    // 파일 URL 반환
    public String getFileUrl(String fileName) {
        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    public void deleteFileFromUrl(String url) {
        try {
            URL s3Url = new URL(url);
            String rawKey = s3Url.getPath().substring(1); // "/abc.png" → "abc.png"
            String decodedKey = URLDecoder.decode(rawKey, StandardCharsets.UTF_8); // ← 디코딩 추가
            System.out.println("버킷 이름: " + bucketName);
            System.out.println("삭제 대상 URL: " + url);
            System.out.println("최종 삭제 대상 key = " + decodedKey);

            amazonS3.deleteObject(bucketName, decodedKey);
        } catch (Exception e) {
            throw new CustomException(S3ExceptionResponseStatus.S3_DELETE_FAILED);
        }
    }

    private String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
