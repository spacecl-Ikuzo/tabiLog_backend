package com.ikuzo.tabilog.domain.spot.GoogleMap;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.DirectionsApi;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;
import com.ikuzo.tabilog.domain.spot.Spot;
import com.ikuzo.tabilog.domain.spot.SpotCategory;
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

    public List<GooglePlaceResponse> searchPlaces(String query, String location) {
        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(geoApiContext, query)
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
            
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(origin)
                    .destination(destination)
                    .mode(com.google.maps.model.TravelMode.valueOf(travelMode.toUpperCase()))
                    .await();
            
            return convertToDirectionsResponse(result);
                    
        } catch (Exception e) {
            log.error("경로 조회 실패: {}", e.getMessage());
            return GoogleDirectionsResponse.builder()
                    .status("ERROR")
                    .errorMessage(e.getMessage())
                    .build();
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
}