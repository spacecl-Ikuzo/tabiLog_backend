package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.plan.DailyPlan;
import com.ikuzo.tabilog.domain.plan.DailyPlanRepository;
import com.ikuzo.tabilog.domain.spot.Spot;
import com.ikuzo.tabilog.domain.spot.SpotRepository;
import com.ikuzo.tabilog.domain.spot.TravelSegment;
import com.ikuzo.tabilog.domain.spot.TravelSegmentRepository;
import com.ikuzo.tabilog.dto.request.SpotRequest;
import com.ikuzo.tabilog.dto.response.SpotResponse;
import com.ikuzo.tabilog.exception.DailyPlanNotFoundException;
import com.ikuzo.tabilog.exception.SpotNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotService {

    private final SpotRepository spotRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final TravelSegmentRepository travelSegmentRepository;

    @Transactional
    public SpotResponse addSpotToDailyPlan(Long dailyPlanId, SpotRequest request) {
        DailyPlan dailyPlan = dailyPlanRepository.findById(dailyPlanId)
                .orElseThrow(() -> new DailyPlanNotFoundException(dailyPlanId));

        // 방문 순서가 중복되지 않도록 조정
        Integer adjustedOrder = adjustVisitOrder(dailyPlanId, request.getVisitOrder());

        Spot spot = Spot.builder()
                .name(request.getName())
                .address(request.getAddress())
                .category(request.getCategory())
                .visitOrder(adjustedOrder)
                .duration(request.getDuration())
                .cost(request.getCost())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .dailyPlan(dailyPlan)
                .build();

        Spot savedSpot = spotRepository.save(spot);
        dailyPlan.addSpot(savedSpot);

        return convertToResponse(savedSpot);
    }

    public SpotResponse getSpot(Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException(spotId));
        return convertToResponse(spot);
    }

    public List<SpotResponse> getSpotsByDailyPlan(Long dailyPlanId) {
        List<Spot> spots = spotRepository.findAllByDailyPlanIdOrderByVisitOrderAsc(dailyPlanId);
        return spots.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<SpotResponse> searchSpots(String name) {
        List<Spot> spots = spotRepository.findByNameContaining(name);
        return spots.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SpotResponse updateSpot(Long spotId, SpotRequest request) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException(spotId));

        spot.updateSpot(request.getName(), request.getAddress(), request.getCategory(),
                       request.getDuration(), request.getCost(), 
                       request.getLatitude(), request.getLongitude());

        // 방문 순서가 변경된 경우 재정렬
        if (!spot.getVisitOrder().equals(request.getVisitOrder())) {
            spot.updateVisitOrder(request.getVisitOrder());
            reorderSpots(spot.getDailyPlan().getId());
        }

        return convertToResponse(spot);
    }

    @Transactional
    public void deleteSpot(Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException(spotId));

        Long dailyPlanId = spot.getDailyPlan().getId();
        spotRepository.delete(spot);
        
        // 삭제 후 순서 재정렬
        reorderSpots(dailyPlanId);
    }

    @Transactional
    public void reorderSpots(Long dailyPlanId) {
        List<Spot> spots = spotRepository.findAllByDailyPlanIdOrderByVisitOrderAsc(dailyPlanId);
        
        for (int i = 0; i < spots.size(); i++) {
            spots.get(i).updateVisitOrder(i);
        }
    }

    private Integer adjustVisitOrder(Long dailyPlanId, Integer requestedOrder) {
        Integer maxOrder = spotRepository.findMaxVisitOrderByDailyPlanId(dailyPlanId);
        return Math.max(requestedOrder, maxOrder + 1);
    }

    private SpotResponse convertToResponse(Spot spot) {
        return SpotResponse.builder()
                .id(spot.getId())
                .name(spot.getName())
                .address(spot.getAddress())
                .category(spot.getCategory())
                .visitOrder(spot.getVisitOrder())
                .duration(spot.getDuration())
                .cost(spot.getCost())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .createdAt(spot.getCreatedAt())
                .updatedAt(spot.getUpdatedAt())
                .build();
    }
}
