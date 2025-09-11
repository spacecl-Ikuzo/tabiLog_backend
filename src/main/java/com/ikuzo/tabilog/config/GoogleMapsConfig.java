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
        if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_GOOGLE_MAPS_API_KEY_HERE".equals(apiKey)) {
            log.warn("Google Maps API Key가 설정되지 않았습니다. Mock 데이터가 사용됩니다.");
            // API 키가 없어도 Context는 생성하되, 실제 API 호출시 예외가 발생하면 Mock 데이터 사용
            return new GeoApiContext.Builder()
                    .apiKey("dummy-key")
                    .build();
        }
        
        log.info("Google Maps API Key가 설정되었습니다.");
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }
}
