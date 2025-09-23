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

        // 1. 북일본 (홋카이도 + 도호쿠 지방)
        regions.put("北日本", Arrays.asList(
                "北海道", // 홋카이도
                "青森県", // 아오모리현
                "岩手県", // 이와테현
                "宮城県", // 미야기현 (센다이)
                "秋田県", // 아키타현
                "山形県", // 야마가타현
                "福島県"  // 후쿠시마현
        ));

        // 2. 동일본 (간토 + 주부 지방)
        regions.put("東日本", Arrays.asList(
                "東京都", // 도쿄도
                "神奈川県", // 가나가와현
                "埼玉県", // 사이타마현
                "千葉県", // 치바현
                "茨城県", // 이바라키현
                "栃木県", // 도치기현
                "群馬県", // 군마현
                "山梨県", // 야마나시현
                "長野県", // 나가노현
                "新潟県", // 니가타현
                "富山県", // 도야마현
                "石川県", // 이시카와현
                "福井県", // 후쿠이현
                "静岡県", // 시즈오카현
                "愛知県"  // 아이치현
        ));
        
        /// 3. 서일본 (간사이 + 주고쿠 + 시코쿠 지방)
        regions.put("西日本", Arrays.asList(
                "大阪府", // 오사카부
                "京都府", // 교토부
                "兵庫県", // 효고현
                "奈良県", // 나라현
                "三重県", // 미에현
                "滋賀県", // 시가현
                "和歌山県", // 와카야마현
                "広島県", // 히로시마현
                "岡山県", // 오카야마현
                "鳥取県", // 돗토리현
                "島根県", // 시마네현
                "山口県", // 야마구치현
                "香川県", // 가가와현 (시코쿠)
                "徳島県", // 도쿠시마현 (시코쿠)
                "愛媛県", // 에히메현 (시코쿠)
                "高知県"  // 고치현 (시코쿠)
        ));

        // 4. 남일본 (규슈 + 오키나와 지방)
        regions.put("南日本", Arrays.asList(
                "福岡県", // 후쿠오카현
                "熊本県", // 구마모토현
                "長崎県", // 나가사키현
                "佐賀県", // 사가현
                "大分県", // 오이타현
                "宮崎県", // 미야자키현
                "鹿児島県", // 가고시마현
                "沖縄県"  // 오키나와현
        ));
        
        return regions;
    }
}
