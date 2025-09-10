package com.ikuzo.tabilog.config;

import com.google.maps.GeoApiContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GoogleMapsConfig {

    @Value("${google.maps.api-key:}")
    private String apiKey;

    @Bean
    public GeoApiContext geoApiContext() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Google Maps API 키가 설정되지 않았습니다. Mock 데이터를 사용합니다.");
        } else {
            log.info("Google Maps API 키가 설정되었습니다. 키 길이: {}", apiKey.length());
        }
        
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }
}
