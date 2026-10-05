package com.library.management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:library-book-covers-bucket}")
    private String bucketName;

    @Value("${file.upload-dir:uploads/covers}")
    private String uploadDir;

    @Autowired
    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadCoverImage(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            return uploadToS3(fileName, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (Exception e) {
            log.warn("S3 upload failed for file {}: {}. Falling back to local filesystem storage.", fileName, e.getMessage());
            return uploadToLocal(fileName, file);
        }
    }

    public String uploadFile(String fileName, byte[] content, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
            log.info("Successfully uploaded file {} to S3 bucket {}", fileName, bucketName);
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
        } catch (Exception e) {
            log.warn("S3 upload failed for {}: {}. Falling back to local storage.", fileName, e.getMessage());
            return uploadToLocalFile(fileName, content);
        }
    }

    private String uploadToS3(String fileName, InputStream inputStream, long contentLength, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
        log.info("Successfully uploaded cover image {} to S3 bucket {}", fileName, bucketName);
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
    }

    private String uploadToLocal(String fileName, MultipartFile file) {
        try {
            Path targetPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetPath);
            Path filePath = targetPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Successfully saved file to local path: {}", filePath);
            return "/uploads/covers/" + fileName;
        } catch (IOException ioEx) {
            log.error("Failed to save file to local filesystem: {}", ioEx.getMessage(), ioEx);
            return "/covers/default-book-cover.png";
        }
    }

    private String uploadToLocalFile(String fileName, byte[] content) {
        try {
            Path targetPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetPath);
            Path filePath = targetPath.resolve(fileName);
            Files.write(filePath, content);
            log.info("Successfully saved file to local path: {}", filePath);
            return "/uploads/covers/" + fileName;
        } catch (IOException ioEx) {
            log.error("Failed to save file to local filesystem: {}", ioEx.getMessage(), ioEx);
            return "/covers/default-book-cover.png";
        }
    }
}
