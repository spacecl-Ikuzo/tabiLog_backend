package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.plan.DailyPlan;
import com.ikuzo.tabilog.domain.plan.DailyPlanRepository;
import com.ikuzo.tabilog.domain.plan.Plan;
import com.ikuzo.tabilog.domain.plan.PlanMember;
import com.ikuzo.tabilog.domain.plan.PlanRepository;
import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import com.ikuzo.tabilog.dto.request.DailyPlanRequest;
import com.ikuzo.tabilog.dto.request.PlanRequest;
import com.ikuzo.tabilog.dto.response.DailyPlanResponse;
import com.ikuzo.tabilog.dto.response.PlanMemberResponse;
import com.ikuzo.tabilog.dto.response.PlanResponse;
import com.ikuzo.tabilog.exception.PlanNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {

    private final PlanRepository planRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final UserRepository userRepository;

    @Transactional
    public PlanResponse createPlan(PlanRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        Plan plan = Plan.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalBudget(request.getTotalBudget())
                .user(user)
                .build();

        Plan savedPlan = planRepository.save(plan);

        // 일별 계획 생성
        if (request.getDailyPlans() != null) {
            for (DailyPlanRequest dailyPlanRequest : request.getDailyPlans()) {
                DailyPlan dailyPlan = DailyPlan.builder()
                        .plan(savedPlan)
                        .visitDate(dailyPlanRequest.getVisitDate())
                        .departureTime(dailyPlanRequest.getDepartureTime())
                        .build();
                savedPlan.addDailyPlan(dailyPlan);
            }
        }

        return convertToResponse(savedPlan);
    }

    public PlanResponse getPlan(Long planId, Long userId) {
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new PlanNotFoundException(planId));
        return convertToResponse(plan);
    }

    public List<PlanResponse> getUserPlans(Long userId) {
        List<Plan> plans = planRepository.findAllByMemberUserIdOrderByStartDateDesc(userId);
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanResponse> getActivePlans(Long userId) {
        List<Plan> plans = planRepository.findActivePlansByMemberUserId(userId);
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanResponse> getUpcomingPlans(Long userId) {
        List<Plan> plans = planRepository.findUpcomingPlansByMemberUserId(userId);
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanResponse> getCompletedPlans(Long userId) {
        List<Plan> plans = planRepository.findCompletedPlansByMemberUserId(userId);
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PlanResponse updatePlan(Long planId, PlanRequest request, Long userId) {
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        plan.updatePlan(request.getTitle(), request.getStartDate(), 
                       request.getEndDate(), request.getTotalBudget());

        return convertToResponse(plan);
    }

    @Transactional
    public void deletePlan(Long planId, Long userId) {
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new PlanNotFoundException(planId));
        planRepository.delete(plan);
    }

    // 사용자의 여행 계획을 prefecture와 status로 필터링하여 조회
    public List<PlanResponse> getUserPlansWithFilters(Long userId, String prefecture, String status) {
        return getUserPlansWithFilters(userId, prefecture, null, status);
    }
    
    // 사용자의 여행 계획을 prefecture(단일 또는 복수)와 status로 필터링하여 조회
    public List<PlanResponse> getUserPlansWithFilters(Long userId, String prefecture, List<String> prefectures, String status) {
        List<Plan> plans = planRepository.findAllByMemberUserIdOrderByStartDateDesc(userId);
        
        // prefecture 필터링 (단일 prefecture 또는 복수 prefectures 중 하나만 적용)
        if (!isEmptyOrNull(prefecture)) {
            // 단일 prefecture 필터링
            plans = plans.stream()
                    .filter(plan -> plan.getPrefecture().equals(prefecture))
                    .collect(Collectors.toList());
        } else if (prefectures != null && !prefectures.isEmpty()) {
            // 복수 prefectures 필터링 (IN 조건)
            plans = plans.stream()
                    .filter(plan -> prefectures.contains(plan.getPrefecture()))
                    .collect(Collectors.toList());
        }
        
        // status 필터링
        if (!isEmptyOrNull(status)) {
            plans = plans.stream()
                    .filter(plan -> plan.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

   
    // 공개된 여행 계획 조회 (필터링 지원)
    public List<PlanResponse> getPublicPlans(String region, String prefecture, String status) {
        List<Plan> plans;
        
        // 모든 필터가 null이거나 빈 문자열인 경우 전체 조회
        if (isEmptyOrNull(region) && isEmptyOrNull(prefecture) && isEmptyOrNull(status)) {
            plans = planRepository.findAllPublicPlans();
        } else {
            // null이나 빈 문자열을 null로 변환
            String regionFilter = isEmptyOrNull(region) ? null : region;
            String prefectureFilter = isEmptyOrNull(prefecture) ? null : prefecture;
            String statusFilter = isEmptyOrNull(status) ? null : status;
            
            plans = planRepository.findPublicPlansWithFilters(regionFilter, prefectureFilter, statusFilter);
        }
        
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private boolean isEmptyOrNull(String value) {
        return value == null || value.trim().isEmpty() || value.equals("전체");
    }

    // Prefecture별 기본 이미지 URL 반환
    private String getPrefectureImageUrl(String prefecture, String customImageUrl) {
        // 커스텀 이미지가 있으면 우선 사용
        if (customImageUrl != null && !customImageUrl.trim().isEmpty()) {
            return customImageUrl;
        }
        
        // Prefecture별 기본 이미지 매핑 (실제 사용하는 현들만)
        Map<String, String> prefectureImages = new HashMap<>();
        
        // 동일본 지역
        prefectureImages.put("北海道", "/images/prefectures/hokkaido.jpg");
        prefectureImages.put("東京都", "/images/prefectures/tokyo.jpg");
        prefectureImages.put("神奈川県", "/images/prefectures/kanagawa.jpg");
        prefectureImages.put("埼玉県", "/images/prefectures/saitama.jpg");
        prefectureImages.put("千葉県", "/images/prefectures/chiba.jpg");
        prefectureImages.put("静岡県", "/images/prefectures/shizuoka.jpg");
        prefectureImages.put("愛知県", "/images/prefectures/aichi.jpg");
        prefectureImages.put("長野県", "/images/prefectures/nagano.jpg");
        
        // 서일본 지역
        prefectureImages.put("京都府", "/images/prefectures/kyoto.jpg");
        prefectureImages.put("大阪府", "/images/prefectures/osaka.jpg");
        prefectureImages.put("兵庫県", "/images/prefectures/hyogo.jpg");
        prefectureImages.put("奈良県", "/images/prefectures/nara.jpg");
        prefectureImages.put("広島県", "/images/prefectures/hiroshima.jpg");
        prefectureImages.put("福岡県", "/images/prefectures/fukuoka.jpg");
        prefectureImages.put("熊本県", "/images/prefectures/kumamoto.jpg");
        prefectureImages.put("沖縄県", "/images/prefectures/okinawa.jpg");
        
        // 북일본 지역 (北海道는 동일본과 중복이므로 제외)
        prefectureImages.put("青森県", "/images/prefectures/aomori.jpg");
        prefectureImages.put("宮城県", "/images/prefectures/miyagi.jpg");
        
        // 남일본 지역 (福岡県, 熊本県은 서일본과 중복이므로 제외)
        prefectureImages.put("長崎県", "/images/prefectures/nagasaki.jpg");
        prefectureImages.put("鹿児島県", "/images/prefectures/kagoshima.jpg");
        
        return prefectureImages.getOrDefault(prefecture, "/images/prefectures/default.jpg");
    }

    private PlanResponse convertToResponse(Plan plan) {
        List<DailyPlanResponse> dailyPlanResponses = plan.getDailyPlans().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        List<PlanMemberResponse> memberResponses = plan.getPlanMembers().stream()
                .map(this::convertToMemberResponse)
                .collect(Collectors.toList());

        return PlanResponse.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .region(plan.getRegion())
                .prefecture(plan.getPrefecture())
                .prefectureImageUrl(getPrefectureImageUrl(plan.getPrefecture(), plan.getPrefectureImageUrl()))
                .participant_count(plan.getParticipant_count())
                .totalBudget(plan.getTotalBudget())
                .status(plan.getStatus())
                .userId(plan.getUser().getId())
                .dailyPlans(dailyPlanResponses)
                .members(memberResponses)
                .isPublic(plan.isPublic())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private DailyPlanResponse convertToResponse(DailyPlan dailyPlan) {
        return DailyPlanResponse.builder()
                .id(dailyPlan.getId())
                .visitDate(dailyPlan.getVisitDate())
                .departureTime(dailyPlan.getDepartureTime())
                .spots(null) // SpotService에서 처리
                .travelSegments(null) // TravelSegmentService에서 처리
                .createdAt(dailyPlan.getCreatedAt())
                .updatedAt(dailyPlan.getUpdatedAt())
                .build();
    }

    private PlanMemberResponse convertToMemberResponse(PlanMember planMember) {
        return PlanMemberResponse.builder()
                .id(planMember.getId())
                .userId(planMember.getUser().getId())
                .userNickname(planMember.getUser().getNickname())
                .userEmail(planMember.getUser().getEmail())
                .role(planMember.getRole())
                .build();
    }
}
