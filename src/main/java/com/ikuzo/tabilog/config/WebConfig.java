package com.ikuzo.tabilog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Google Cloud Storage를 사용하므로 로컬 정적 리소스 설정 제거
    // 이미지는 GCS에서 직접 서빙됩니다
}
