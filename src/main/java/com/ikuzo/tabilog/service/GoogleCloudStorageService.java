package com.ikuzo.tabilog.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class GoogleCloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudStorageService.class);
    
    private final Storage storage;
    
    @Value("${gcs.bucket-name:tabilog-images}")
    private String bucketName;

    public GoogleCloudStorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public String uploadImage(MultipartFile file) throws IOException {
        try {
            // 파일명 생성 (UUID + 원본 파일명)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;
            
            logger.info("GCS 버킷에 이미지 업로드 시작: {}", bucketName);
            
            // Blob 정보 생성
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            
            // 파일 업로드
            storage.create(blobInfo, file.getBytes());
            
            // 공개 URL 생성
            String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
            
            logger.info("이미지 업로드 성공: {}", publicUrl);
            
            return publicUrl;
            
        } catch (Exception e) {
            logger.error("GCS 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new IOException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}