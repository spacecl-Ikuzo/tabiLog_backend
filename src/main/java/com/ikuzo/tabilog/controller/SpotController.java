package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.domain.spot.Spot;
import com.ikuzo.tabilog.domain.spot.GoogleMap.GoogleMapsService;
import com.ikuzo.tabilog.dto.response.GooglePlaceResponse;
import com.ikuzo.tabilog.dto.response.GoogleDirectionsResponse;
import com.ikuzo.tabilog.dto.request.SpotRequest;
import com.ikuzo.tabilog.dto.response.SpotResponse;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.SpotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;
    private final GoogleMapsService googleMapsService;

    @PostMapping("/daily-plans/{dailyPlanId}")
    public ResponseEntity<ApiResponse<SpotResponse>> addSpotToDailyPlan(
            @PathVariable Long dailyPlanId,
            @Valid @RequestBody SpotRequest request) {
        
        SpotResponse response = spotService.addSpotToDailyPlan(dailyPlanId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("관광지가 추가되었습니다.", response));
    }

    @GetMapping("/{spotId}")
    public ResponseEntity<ApiResponse<SpotResponse>> getSpot(@PathVariable Long spotId) {
        SpotResponse response = spotService.getSpot(spotId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/daily-plans/{dailyPlanId}")
    public ResponseEntity<ApiResponse<List<SpotResponse>>> getSpotsByDailyPlan(
            @PathVariable Long dailyPlanId) {
        
        List<SpotResponse> responses = spotService.getSpotsByDailyPlan(dailyPlanId);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SpotResponse>>> searchSpots(
            @RequestParam String query) {
        
        List<SpotResponse> responses = spotService.searchSpots(query);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/travel-time")
    public ResponseEntity<ApiResponse<String>> getTravelTime(
            @RequestParam double lat1,
            @RequestParam double lng1,
            @RequestParam double lat2,
            @RequestParam double lng2) {
        
        String travelTime = googleMapsService.getTravelTime(lat1, lng1, lat2, lng2);
        
        return ResponseEntity.ok(ApiResponse.success(travelTime));
    }

    @GetMapping("/address")
    public ResponseEntity<ApiResponse<String>> getFormattedAddress(
            @RequestParam double lat,
            @RequestParam double lng) {
        
        String address = googleMapsService.getFormattedAddress(lat, lng);
        
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PutMapping("/{spotId}")
    public ResponseEntity<ApiResponse<SpotResponse>> updateSpot(
            @PathVariable Long spotId,
            @Valid @RequestBody SpotRequest request) {
        
        SpotResponse response = spotService.updateSpot(spotId, request);
        
        return ResponseEntity.ok(ApiResponse.success("관광지가 수정되었습니다.", response));
    }

    @DeleteMapping("/{spotId}")
    public ResponseEntity<ApiResponse<Void>> deleteSpot(@PathVariable Long spotId) {
        spotService.deleteSpot(spotId);
        
        return ResponseEntity.ok(ApiResponse.success("관광지가 삭제되었습니다.", null));
    }

    @PutMapping("/daily-plans/{dailyPlanId}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderSpots(@PathVariable Long dailyPlanId) {
        spotService.reorderSpots(dailyPlanId);
        
        return ResponseEntity.ok(ApiResponse.success("관광지 순서가 재정렬되었습니다.", null));
    }

    @GetMapping("/google-search")
    public ResponseEntity<ApiResponse<List<GooglePlaceResponse>>> searchPlaces(
            @RequestParam String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "ja") String language,
            @RequestParam(required = false, defaultValue = "JP") String region) {
        
        List<GooglePlaceResponse> places = googleMapsService.searchPlaces(query, location, language, region);
        
        return ResponseEntity.ok(ApiResponse.success(places));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<GooglePlaceResponse>>> getNearbyPlaces(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "tourist_attraction") String type) {
        
        List<GooglePlaceResponse> places = googleMapsService.getNearbyPlaces(lat, lng, type);
        
        return ResponseEntity.ok(ApiResponse.success(places));
    }

    @GetMapping("/directions")
    public ResponseEntity<ApiResponse<GoogleDirectionsResponse>> getDirections(
            @RequestParam double lat1,
            @RequestParam double lng1,
            @RequestParam double lat2,
            @RequestParam double lng2,
            @RequestParam(required = false, defaultValue = "WALKING") String travelMode) {
        
        GoogleDirectionsResponse directions = googleMapsService.getDirections(lat1, lng1, lat2, lng2, travelMode);
        
        return ResponseEntity.ok(ApiResponse.success(directions));
    }
}
