package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.service.GoogleCloudStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    
    @Autowired
    private GoogleCloudStorageService storageService;

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 파일이 비어있는지 확인
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일이 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 파일인지 확인
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 파일 크기 확인 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "파일 크기는 5MB를 초과할 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // Google Cloud Storage에 업로드
            String imageUrl = storageService.uploadImage(file);
            logger.info("이미지 업로드 성공: {}", imageUrl);

            // 응답 데이터 구성
            response.put("success", true);
            response.put("message", "이미지 업로드 성공");
            response.put("url", imageUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("이미지 업로드 실패: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
