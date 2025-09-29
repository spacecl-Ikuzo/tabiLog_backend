package com.ikuzo.tabilog.service;

import com.google.cloud.storage.Blob;
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
    
    @Value("${gcs.bucket-name:tabilog-images}")
    private String bucketName;
    
    private final Storage storage;

    public GoogleCloudStorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public String uploadImage(MultipartFile file) throws IOException {
        try {
            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // Blob 정보 생성
            BlobId blobId = BlobId.of(bucketName, filename);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // 파일 업로드
            Blob blob = storage.create(blobInfo, file.getBytes());
            
            logger.info("이미지가 Google Cloud Storage에 업로드됨: {}", blob.getMediaLink());
            
            // 공개 URL 반환
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, filename);
            
        } catch (Exception e) {
            logger.error("Google Cloud Storage 업로드 실패: {}", e.getMessage(), e);
            throw new IOException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            // URL에서 파일명 추출
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            BlobId blobId = BlobId.of(bucketName, filename);
            
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                logger.info("이미지가 Google Cloud Storage에서 삭제됨: {}", filename);
            } else {
                logger.warn("이미지 삭제 실패: {}", filename);
            }
        } catch (Exception e) {
            logger.error("Google Cloud Storage 삭제 실패: {}", e.getMessage(), e);
        }
    }
}
