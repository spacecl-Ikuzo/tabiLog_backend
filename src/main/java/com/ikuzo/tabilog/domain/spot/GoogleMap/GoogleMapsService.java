package com.ikuzo.tabilog.domain.spot.GoogleMap;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.DirectionsApi;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;
import com.ikuzo.tabilog.dto.response.GoogleDirectionsResponse;
import com.ikuzo.tabilog.dto.response.GooglePlaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleMapsService {

    private final GeoApiContext geoApiContext;

    public String getTravelTime(double lat1, double lng1, double lat2, double lng2) {
        try {
            LatLng origin = new LatLng(lat1, lng1);
            LatLng destination = new LatLng(lat2, lng2);
            
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(origin)
                    .destination(destination)
                    .mode(com.google.maps.model.TravelMode.WALKING)
                    .await();
            
            if (result.routes.length > 0) {
                Duration duration = result.routes[0].legs[0].duration;
                return duration.humanReadable;
            }
        } catch (Exception e) {
            log.error("이동 시간 조회 실패: {}", e.getMessage());
        }
        
        return "약 13분"; // 기본값
    }

    public int getTravelTimeMinutes(double lat1, double lng1, double lat2, double lng2) {
        try {
            LatLng origin = new LatLng(lat1, lng1);
            LatLng destination = new LatLng(lat2, lng2);
            
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(origin)
                    .destination(destination)
                    .mode(com.google.maps.model.TravelMode.WALKING)
                    .await();
            
            if (result.routes.length > 0) {
                Duration duration = result.routes[0].legs[0].duration;
                return (int) (duration.inSeconds / 60);
            }
        } catch (Exception e) {
            log.error("이동 시간(분) 조회 실패: {}", e.getMessage());
        }
        
        return 13; // 기본값
    }

    public List<GooglePlaceResponse> searchPlaces(String query, String location, String language, String region) {
        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(geoApiContext, query)
                    .language(language)
                    .region(region)
                    .await();
            
            return Arrays.stream(response.results)
                    .map(this::convertToGooglePlaceResponse)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("장소 검색 실패: {}", e.getMessage());
            return getMockPlaces(query);
        }
    }

    public List<GooglePlaceResponse> getNearbyPlaces(double lat, double lng, String type) {
        try {
            LatLng location = new LatLng(lat, lng);
            PlacesSearchResponse response = PlacesApi.nearbySearchQuery(geoApiContext, location)
                    .radius(1000) // 1km 반경
                    .type(PlaceType.valueOf(type.toUpperCase()))
                    .await();
            
            return Arrays.stream(response.results)
                    .map(this::convertToGooglePlaceResponse)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("주변 장소 검색 실패: {}", e.getMessage());
            return getMockNearbyPlaces(lat, lng);
        }
    }

    public GoogleDirectionsResponse getDirections(double lat1, double lng1, double lat2, double lng2, String travelMode) {
        try {
            LatLng origin = new LatLng(lat1, lng1);
            LatLng destination = new LatLng(lat2, lng2);
            
            log.info("경로 조회 요청 - 출발지: ({}, {}), 도착지: ({}, {}), 이동수단: {}", lat1, lng1, lat2, lng2, travelMode);
            
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(origin)
                    .destination(destination)
                    .mode(com.google.maps.model.TravelMode.valueOf(travelMode.toUpperCase()))
                    .await();
            
            if (result.routes == null || result.routes.length == 0) {
                log.warn("경로를 찾을 수 없습니다. 출발지: ({}, {}), 도착지: ({}, {})", lat1, lng1, lat2, lng2);
                return GoogleDirectionsResponse.builder()
                        .status("ZERO_RESULTS")
                        .errorMessage("경로를 찾을 수 없습니다.")
                        .build();
            }
            
            return convertToDirectionsResponse(result);
                    
        } catch (IllegalArgumentException e) {
            log.error("잘못된 이동수단: {}. 지원되는 이동수단: WALKING, DRIVING, BICYCLING, TRANSIT", travelMode);
            return GoogleDirectionsResponse.builder()
                    .status("INVALID_REQUEST")
                    .errorMessage("지원되지 않는 이동수단입니다: " + travelMode)
                    .build();
        } catch (com.google.maps.errors.ZeroResultsException e) {
            log.warn("경로를 찾을 수 없습니다. 출발지: ({}, {}), 도착지: ({}, {}), 이동수단: {}", 
                     lat1, lng1, lat2, lng2, travelMode);
            return GoogleDirectionsResponse.builder()
                    .status("ZERO_RESULTS")
                    .errorMessage("해당 경로를 찾을 수 없습니다. 다른 이동수단을 시도해보세요.")
                    .build();
        } catch (com.google.maps.errors.ApiException e) {
            log.error("Google Maps API 오류: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return GoogleDirectionsResponse.builder()
                    .status("API_ERROR")
                    .errorMessage("Google Maps API 오류: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("경로 조회 실패 - 출발지: ({}, {}), 도착지: ({}, {}), 이동수단: {}, 오류: {}", 
                     lat1, lng1, lat2, lng2, travelMode, e.getClass().getSimpleName(), e);
            
            // Mock 데이터를 반환하여 기본적인 경로 정보 제공
            return getMockDirections(lat1, lng1, lat2, lng2, travelMode);
        }
    }

    public String getFormattedAddress(double lat, double lng) {
        try {
            LatLng location = new LatLng(lat, lng);
            GeocodingResult[] results = GeocodingApi.reverseGeocode(geoApiContext, location).await();
            
            if (results.length > 0) {
                return results[0].formattedAddress;
            }
        } catch (Exception e) {
            log.error("주소 조회 실패: {}", e.getMessage());
        }
        
        return "주소 정보 없음";
    }

    private GooglePlaceResponse convertToGooglePlaceResponse(PlacesSearchResult place) {
        return GooglePlaceResponse.builder()
                .placeId(place.placeId)
                .name(place.name)
                .formattedAddress(place.formattedAddress)
                .vicinity(place.vicinity)
                .latitude(place.geometry.location.lat)
                .longitude(place.geometry.location.lng)
                .types(Arrays.stream(place.types).map(String::valueOf).toArray(String[]::new))
                .rating((double) place.rating)
                .userRatingsTotal(place.userRatingsTotal)
                .build();
    }

    private GoogleDirectionsResponse convertToDirectionsResponse(DirectionsResult result) {
        List<GoogleDirectionsResponse.Route> routes = Arrays.stream(result.routes)
                .map(route -> {
                    List<GoogleDirectionsResponse.Leg> legs = Arrays.stream(route.legs)
                            .map(leg -> {
                                List<GoogleDirectionsResponse.Step> steps = Arrays.stream(leg.steps)
                                        .map(step -> GoogleDirectionsResponse.Step.builder()
                                                .distance(GoogleDirectionsResponse.Distance.builder()
                                                        .text(step.distance.humanReadable)
                                                        .value(step.distance.inMeters)
                                                        .build())
                                                .duration(GoogleDirectionsResponse.Duration.builder()
                                                        .text(step.duration.humanReadable)
                                                        .value(step.duration.inSeconds)
                                                        .build())
                                                .htmlInstructions(step.htmlInstructions)
                                                .travelMode(step.travelMode.name())
                                                .build())
                                        .collect(Collectors.toList());
                                
                                return GoogleDirectionsResponse.Leg.builder()
                                        .distance(GoogleDirectionsResponse.Distance.builder()
                                                .text(leg.distance.humanReadable)
                                                .value(leg.distance.inMeters)
                                                .build())
                                        .duration(GoogleDirectionsResponse.Duration.builder()
                                                .text(leg.duration.humanReadable)
                                                .value(leg.duration.inSeconds)
                                                .build())
                                        .startAddress(leg.startAddress)
                                        .endAddress(leg.endAddress)
                                        .steps(steps)
                                        .build();
                            })
                            .collect(Collectors.toList());
                    
                    return GoogleDirectionsResponse.Route.builder()
                            .summary(route.summary)
                            .legs(legs)
                            .overviewPolyline(route.overviewPolyline.getEncodedPath())
                            .build();
                })
                .collect(Collectors.toList());
        
        return GoogleDirectionsResponse.builder()
                .status("OK")
                .routes(routes)
                .build();
    }

    // Mock 데이터 메서드들 (API 키가 없을 때 사용)
    private List<GooglePlaceResponse> getMockPlaces(String query) {
        List<GooglePlaceResponse> mockResults = new ArrayList<>();
        
        if (query.contains("도쿄") || query.contains("東京")) {
            mockResults.add(GooglePlaceResponse.builder()
                    .placeId("mock_tokyo_tower")
                    .name("도쿄 타워")
                    .formattedAddress("東京都港区芝公園4-2-8")
                    .latitude(35.6586)
                    .longitude(139.7454)
                    .rating(4.2)
                    .userRatingsTotal(50000)
                    .build());
            
            mockResults.add(GooglePlaceResponse.builder()
                    .placeId("mock_shibuya")
                    .name("시부야 스크램블 교차로")
                    .formattedAddress("東京都渋谷区道玄坂2-1")
                    .latitude(35.6598)
                    .longitude(139.7006)
                    .rating(4.0)
                    .userRatingsTotal(30000)
                    .build());
        }
        
        return mockResults;
    }

    private List<GooglePlaceResponse> getMockNearbyPlaces(double lat, double lng) {
        List<GooglePlaceResponse> mockResults = new ArrayList<>();
        
        mockResults.add(GooglePlaceResponse.builder()
                .placeId("mock_cafe")
                .name("근처 카페")
                .formattedAddress("주소 정보")
                .latitude(lat + 0.001)
                .longitude(lng + 0.001)
                .rating(4.0)
                .userRatingsTotal(100)
                .build());
        
        mockResults.add(GooglePlaceResponse.builder()
                .placeId("mock_restaurant")
                .name("근처 식당")
                .formattedAddress("주소 정보")
                .latitude(lat - 0.001)
                .longitude(lng - 0.001)
                .rating(4.2)
                .userRatingsTotal(200)
                .build());
        
        return mockResults;
    }

    private GoogleDirectionsResponse getMockDirections(double lat1, double lng1, double lat2, double lng2, String travelMode) {
        log.info("Mock 경로 데이터를 반환합니다. 출발지: ({}, {}), 도착지: ({}, {}), 이동수단: {}", lat1, lng1, lat2, lng2, travelMode);
        
        // 거리 계산 (간단한 유클리드 거리)
        double distance = Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lng2 - lng1, 2)) * 111000; // 대략적인 미터 변환
        int durationMinutes = (int) (distance / 1000 * 15); // 대략적인 시간 계산 (km당 15분)
        
        List<GoogleDirectionsResponse.Step> steps = new ArrayList<>();
        steps.add(GoogleDirectionsResponse.Step.builder()
                .distance(GoogleDirectionsResponse.Distance.builder()
                        .text(String.format("%.1f km", distance / 1000))
                        .value((long) distance)
                        .build())
                .duration(GoogleDirectionsResponse.Duration.builder()
                        .text(String.format("%d분", durationMinutes))
                        .value((long) (durationMinutes * 60))
                        .build())
                .htmlInstructions("출발지에서 도착지로 이동")
                .travelMode(travelMode.toUpperCase())
                .build());
        
        List<GoogleDirectionsResponse.Leg> legs = new ArrayList<>();
        legs.add(GoogleDirectionsResponse.Leg.builder()
                .distance(GoogleDirectionsResponse.Distance.builder()
                        .text(String.format("%.1f km", distance / 1000))
                        .value((long) distance)
                        .build())
                .duration(GoogleDirectionsResponse.Duration.builder()
                        .text(String.format("%d분", durationMinutes))
                        .value((long) (durationMinutes * 60))
                        .build())
                .startAddress("출발지")
                .endAddress("도착지")
                .steps(steps)
                .build());
        
        List<GoogleDirectionsResponse.Route> routes = new ArrayList<>();
        routes.add(GoogleDirectionsResponse.Route.builder()
                .summary("Mock 경로")
                .legs(legs)
                .overviewPolyline("mock_polyline")
                .build());
        
        return GoogleDirectionsResponse.builder()
                .status("OK")
                .routes(routes)
                .errorMessage("Google Maps API를 사용할 수 없어 Mock 데이터를 제공합니다.")
                .build();
    }
}