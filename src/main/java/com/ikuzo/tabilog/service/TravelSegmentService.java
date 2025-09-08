package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.plan.DailyPlan;
import com.ikuzo.tabilog.domain.plan.DailyPlanRepository;
import com.ikuzo.tabilog.domain.spot.Spot;
import com.ikuzo.tabilog.domain.spot.SpotRepository;
import com.ikuzo.tabilog.domain.spot.TravelSegment;
import com.ikuzo.tabilog.domain.spot.TravelSegmentRepository;
import com.ikuzo.tabilog.dto.request.TravelSegmentRequest;
import com.ikuzo.tabilog.dto.response.TravelSegmentResponse;
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
public class TravelSegmentService {

    private final TravelSegmentRepository travelSegmentRepository;
    private final SpotRepository spotRepository;
    private final DailyPlanRepository dailyPlanRepository;

    @Transactional
    public TravelSegmentResponse createTravelSegment(TravelSegmentRequest request) {
        Spot fromSpot = spotRepository.findById(request.getFromSpotId())
                .orElseThrow(() -> new SpotNotFoundException(request.getFromSpotId()));
        
        Spot toSpot = spotRepository.findById(request.getToSpotId())
                .orElseThrow(() -> new SpotNotFoundException(request.getToSpotId()));

        DailyPlan dailyPlan = fromSpot.getDailyPlan();

        TravelSegment travelSegment = TravelSegment.builder()
                .fromSpot(fromSpot)
                .toSpot(toSpot)
                .duration(request.getDuration())
                .travelMode(request.getTravelMode())
                .segmentOrder(request.getSegmentOrder())
                .dailyPlan(dailyPlan)
                .build();

        TravelSegment savedSegment = travelSegmentRepository.save(travelSegment);
        dailyPlan.addTravelSegment(savedSegment);

        return convertToResponse(savedSegment);
    }

    public List<TravelSegmentResponse> getTravelSegmentsByDailyPlan(Long dailyPlanId) {
        List<TravelSegment> segments = travelSegmentRepository.findAllByDailyPlanIdOrderBySegmentOrderAsc(dailyPlanId);
        return segments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public TravelSegmentResponse getTravelSegment(Long segmentId) {
        TravelSegment segment = travelSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new RuntimeException("이동 구간을 찾을 수 없습니다: " + segmentId));
        return convertToResponse(segment);
    }

    @Transactional
    public TravelSegmentResponse updateTravelSegment(Long segmentId, TravelSegmentRequest request) {
        TravelSegment segment = travelSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new RuntimeException("이동 구간을 찾을 수 없습니다: " + segmentId));

        segment.updateTravelSegment(request.getDuration(), request.getTravelMode());

        // 구간 순서가 변경된 경우 재정렬
        if (!segment.getSegmentOrder().equals(request.getSegmentOrder())) {
            segment.updateSegmentOrder(request.getSegmentOrder());
            reorderTravelSegments(segment.getDailyPlan().getId());
        }

        return convertToResponse(segment);
    }

    @Transactional
    public void deleteTravelSegment(Long segmentId) {
        TravelSegment segment = travelSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new RuntimeException("이동 구간을 찾을 수 없습니다: " + segmentId));

        Long dailyPlanId = segment.getDailyPlan().getId();
        travelSegmentRepository.delete(segment);
        
        // 삭제 후 순서 재정렬
        reorderTravelSegments(dailyPlanId);
    }

    @Transactional
    public void reorderTravelSegments(Long dailyPlanId) {
        List<TravelSegment> segments = travelSegmentRepository.findAllByDailyPlanIdOrderBySegmentOrderAsc(dailyPlanId);
        
        for (int i = 0; i < segments.size(); i++) {
            segments.get(i).updateSegmentOrder(i);
        }
    }

    private TravelSegmentResponse convertToResponse(TravelSegment segment) {
        return TravelSegmentResponse.builder()
                .id(segment.getId())
                .fromSpotId(segment.getFromSpot().getId())
                .fromSpotName(segment.getFromSpot().getName())
                .toSpotId(segment.getToSpot().getId())
                .toSpotName(segment.getToSpot().getName())
                .duration(segment.getDuration())
                .travelMode(segment.getTravelMode())
                .segmentOrder(segment.getSegmentOrder())
                .createdAt(segment.getCreatedAt())
                .updatedAt(segment.getUpdatedAt())
                .build();
    }
}
