package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/categories/regions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://localhost:8080", "http://127.0.0.1:3000", "http://127.0.0.1:5173"}, 
             allowCredentials = "true",
             allowedHeaders = "*",
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class RegionController {


    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getPrefecturesByRegion(
            @RequestParam(required = false, defaultValue = "") String region) {
        
        Map<String, List<String>> regions = getRegionMappings();
        
        // 빈값 또는 "전체"인 경우 모든 지역의 도시 반환
        if (region == null || region.trim().isEmpty() || region.equals("전체")) {
            List<String> allPrefectures = regions.values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("전체 지역의 현(prefecture) 정보를 조회했습니다.", allPrefectures));
        }
        
        // 특정 지역의 도시 반환
        List<String> prefectures = regions.get(region);
        if (prefectures == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("존재하지 않는 지역입니다: " + region));
        }
        
        return ResponseEntity.ok(ApiResponse.success(region + " 지역의 현(prefecture) 정보를 조회했습니다.", prefectures));
    }

    @GetMapping("/{region}")
    public ResponseEntity<ApiResponse<List<String>>> getRegionPrefecturesByPath(@PathVariable String region) {
        Map<String, List<String>> regions = getRegionMappings();
        
        List<String> prefectures = regions.get(region);
        if (prefectures == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("존재하지 않는 지역입니다: " + region));
        }
        
        return ResponseEntity.ok(ApiResponse.success(region + " 지역의 현(prefecture) 정보를 조회했습니다.", prefectures));
    }

    // 지역별 대표 현들 매핑 (PlanService와 동일한 도시만 사용)
    private Map<String, List<String>> getRegionMappings() {
        Map<String, List<String>> regions = new HashMap<>();
        
        // 동일본 지역
        regions.put("東日本", Arrays.asList(
            "北海道", "東京都", "神奈川県", "埼玉県", "千葉県", "静岡県", "愛知県", "長野県"
        ));
        
        // 서일본 지역
        regions.put("西日本", Arrays.asList(
            "京都府", "大阪府", "兵庫県", "奈良県", "広島県", "福岡県", "熊本県", "沖縄県"
        ));
        
        // 북일본 지역 (北海道는 동일본과 중복이므로 제외)
        regions.put("北日本", Arrays.asList(
            "青森県", "宮城県"
        ));
        
        // 남일본 지역 (福岡県, 熊本県, 沖縄県은 서일본과 중복이므로 제외)
        regions.put("南日本", Arrays.asList(
            "長崎県", "鹿児島県"
        ));
        
        return regions;
    }
}
