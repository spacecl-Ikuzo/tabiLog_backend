package com.ikuzo.tabilog.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class GoogleCloudStorageService {

    private final Storage storage;
    private final String bucketName = "tabilog-dev"; // 환경 변수로 관리 권장

    public GoogleCloudStorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        
        storage.create(blobInfo, file.getBytes());
        
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }
}