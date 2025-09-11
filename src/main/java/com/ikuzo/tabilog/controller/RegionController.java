package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://localhost:8082", "http://127.0.0.1:3000", "http://127.0.0.1:5173"}, 
             allowCredentials = "true",
             allowedHeaders = "*",
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class RegionController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllRegions() {
        Map<String, Object> regionData = new HashMap<>();
        
        // 일본 지역별 현(prefecture) 매핑
        Map<String, List<String>> regions = new HashMap<>();
        
        regions.put("東日本", Arrays.asList(
            "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県",
            "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県",
            "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県", "静岡県", "愛知県"
        ));
        
        regions.put("西日本", Arrays.asList(
            "三重県", "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県",
            "鳥取県", "島根県", "岡山県", "広島県", "山口県",
            "徳島県", "香川県", "愛媛県", "高知県",
            "福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"
        ));
        
        regions.put("北日本", Arrays.asList(
            "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県"
        ));
        
        regions.put("南日本", Arrays.asList(
            "福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"
        ));
        
        regionData.put("regions", regions);
        regionData.put("allRegions", Arrays.asList("전체", "東日本", "西日本", "北日本", "南日本"));
        
        return ResponseEntity.ok(ApiResponse.success("지역별 관광지 정보를 조회했습니다.", regionData));
    }

    @GetMapping("/{region}/prefectures")
    public ResponseEntity<ApiResponse<List<String>>> getPrefecturesByRegion(@PathVariable String region) {
        Map<String, List<String>> regions = new HashMap<>();
        
        regions.put("東日本", Arrays.asList(
            "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県",
            "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県",
            "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県", "静岡県", "愛知県"
        ));
        
        regions.put("西日本", Arrays.asList(
            "三重県", "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県",
            "鳥取県", "島根県", "岡山県", "広島県", "山口県",
            "徳島県", "香川県", "愛媛県", "高知県",
            "福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"
        ));
        
        regions.put("北日本", Arrays.asList(
            "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県"
        ));
        
        regions.put("南日本", Arrays.asList(
            "福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"
        ));
        
        List<String> prefectures = regions.get(region);
        if (prefectures == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("존재하지 않는 지역입니다: " + region));
        }
        
        return ResponseEntity.ok(ApiResponse.success(region + " 지역의 관광지 정보를 조회했습니다.", prefectures));
    }
}
