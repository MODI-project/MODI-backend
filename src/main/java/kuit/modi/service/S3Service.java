package kuit.modi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            throw new RuntimeException("이미지 파일 업로드 실패", e);
        }

        return getFileUrl(uniqueFilename);
    }

    // 파일 URL 반환
    public String getFileUrl(String fileName) {
        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    // 파일 삭제
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(bucketName, fileName);
    }
}
