package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.plan.DailyPlan;
import com.ikuzo.tabilog.domain.plan.DailyPlanRepository;
import com.ikuzo.tabilog.domain.spot.Spot;
import com.ikuzo.tabilog.domain.spot.SpotRepository;
import com.ikuzo.tabilog.domain.spot.TravelSegmentRepository;
import com.ikuzo.tabilog.domain.expense.Expense;
import com.ikuzo.tabilog.domain.expense.ExpenseCategory;
import com.ikuzo.tabilog.domain.expense.ExpenseRepository;
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
    private final ExpenseRepository expenseRepository;

    @Transactional
    public SpotResponse addSpotToDailyPlan(Long dailyPlanId, SpotRequest request, Long userId) {
        DailyPlan dailyPlan = dailyPlanRepository.findById(dailyPlanId)
                .orElseThrow(() -> new DailyPlanNotFoundException(dailyPlanId));
        
        // DailyPlan의 소유자 확인
        if (!dailyPlan.getPlan().getUser().getId().equals(userId)) {
            throw new RuntimeException("이 DailyPlan에 접근할 권한이 없습니다.");
        }

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

        // Spot cost가 0보다 크면 자동으로 Expense 생성
        if (request.getCost() > 0) {
            Expense expense = Expense.builder()
                    .plan(dailyPlan.getPlan())
                    .spot(savedSpot)
                    .item(savedSpot.getName() + " 입장료")
                    .amount(request.getCost().intValue())
                    .category(ExpenseCategory.SIGHTSEEING)
                    .expenseDate(dailyPlan.getVisitDate())
                    .build();
            expenseRepository.save(expense);
        }

        return convertToResponse(savedSpot);
    }

    public SpotResponse getSpot(Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException(spotId));
        return convertToResponse(spot);
    }

    public List<SpotResponse> getSpotsByDailyPlan(Long dailyPlanId, Long userId) {
        DailyPlan dailyPlan = dailyPlanRepository.findById(dailyPlanId)
                .orElseThrow(() -> new DailyPlanNotFoundException(dailyPlanId));
        
        // 접근 권한 확인
        boolean hasAccess = false;
        
        if (userId != null) {
            // 인증된 사용자인 경우: 자신의 데이터이거나 공개된 데이터
            hasAccess = dailyPlan.getPlan().getUser().getId().equals(userId) || dailyPlan.getPlan().isPublic();
        } else {
            // 인증되지 않은 사용자인 경우: 공개된 데이터만
            hasAccess = dailyPlan.getPlan().isPublic();
        }
        
        if (!hasAccess) {
            throw new RuntimeException("이 DailyPlan에 접근할 권한이 없습니다.");
        }
        
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
    public void deleteSpot(Long spotId, Long userId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException(spotId));

        // Spot의 소유자 확인 (DailyPlan → Plan → User)
        if (!spot.getDailyPlan().getPlan().getUser().getId().equals(userId)) {
            throw new RuntimeException("이 Spot에 접근할 권한이 없습니다.");
        }

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
