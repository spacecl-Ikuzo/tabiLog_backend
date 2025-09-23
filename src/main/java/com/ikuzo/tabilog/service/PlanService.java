package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.plan.DailyPlan;
import com.ikuzo.tabilog.domain.plan.DailyPlanRepository;
import com.ikuzo.tabilog.domain.plan.Plan;
import com.ikuzo.tabilog.domain.plan.PlanMember;
import com.ikuzo.tabilog.domain.plan.PlanMemberRepository;
import com.ikuzo.tabilog.domain.plan.PlanMemberRole;
import com.ikuzo.tabilog.domain.plan.PlanRepository;
import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import com.ikuzo.tabilog.domain.invitation.PlanInvitationRepository;
import com.ikuzo.tabilog.domain.spot.SpotRepository;
import com.ikuzo.tabilog.domain.spot.TravelSegmentRepository;
import com.ikuzo.tabilog.dto.request.DailyPlanRequest;
import com.ikuzo.tabilog.dto.request.PlanRequest;
import com.ikuzo.tabilog.dto.response.DailyPlanResponse;
import com.ikuzo.tabilog.dto.response.ExpenseResponse;
import com.ikuzo.tabilog.dto.response.PlanMemberResponse;
import com.ikuzo.tabilog.dto.response.PlanResponse;
import com.ikuzo.tabilog.dto.response.SpotResponse;
import com.ikuzo.tabilog.dto.response.TravelSegmentResponse;
import com.ikuzo.tabilog.exception.PlanNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private final PlanMemberRepository planMemberRepository;
    private final UserRepository userRepository;
    private final SpotService spotService;
    private final TravelSegmentService travelSegmentService;
    private final ExpenseService expenseService;
    private final SpotRepository spotRepository;
    private final TravelSegmentRepository travelSegmentRepository;
    private final PlanInvitationRepository planInvitationRepository;

    @Transactional
    public PlanResponse createPlan(PlanRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        Plan plan = Plan.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalBudget(request.getTotalBudget())
                .region(request.getRegion())
                .prefecture(request.getPrefecture())
                .prefectureImageUrl(request.getPrefectureImageUrl())
                .participant_count(1L) // 기본값: 1명 (생성자)
                .status("PLANNING") // 기본값: 계획 중
                .isPublic(false) // 기본값: 비공개
                .user(user)
                .build();

        Plan savedPlan = planRepository.save(plan);

        // 계획 작성자를 OWNER로 PlanMember 테이블에 추가
        PlanMember ownerMember = PlanMember.builder()
                .plan(savedPlan)
                .user(user)
                .role(PlanMemberRole.OWNER)
                .build();
        planMemberRepository.save(ownerMember);

        // 일별 계획 생성
        if (request.getDailyPlans() != null && !request.getDailyPlans().isEmpty()) {
            // 요청에 DailyPlan이 있는 경우
            for (DailyPlanRequest dailyPlanRequest : request.getDailyPlans()) {
                DailyPlan dailyPlan = DailyPlan.builder()
                        .plan(savedPlan)
                        .visitDate(dailyPlanRequest.getVisitDate())
                        .departureTime(dailyPlanRequest.getDepartureTime())
                        .build();
                savedPlan.addDailyPlan(dailyPlan);
            }
        } else {
            // 요청에 DailyPlan이 없는 경우, startDate와 endDate를 기반으로 자동 생성
            LocalDate currentDate = savedPlan.getStartDate();
            LocalDate endDate = savedPlan.getEndDate();
            
            while (!currentDate.isAfter(endDate)) {
                DailyPlan dailyPlan = DailyPlan.builder()
                        .plan(savedPlan)
                        .visitDate(currentDate)
                        .departureTime(LocalTime.of(9, 0)) // 기본 출발 시간: 09:00
                        .build();
                savedPlan.addDailyPlan(dailyPlan);
                currentDate = currentDate.plusDays(1);
            }
            
        }
        // DailyPlan들을 데이터베이스에 저장 (ID 생성을 위해)
        planRepository.save(savedPlan);
        return convertToResponse(savedPlan);
    }

    public PlanResponse getPlan(Long planId, Long userId) {
        Plan plan = planRepository.findByIdAndMemberUserIdWithDailyPlans(planId, userId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        // DailyPlan이 없는 경우 자동으로 생성
        if (plan.getDailyPlans().isEmpty()) {
            LocalDate currentDate = plan.getStartDate();
            LocalDate endDate = plan.getEndDate();

            while (!currentDate.isAfter(endDate)) {
                DailyPlan dailyPlan = DailyPlan.builder()
                        .plan(plan)
                        .visitDate(currentDate)
                        .departureTime(LocalTime.of(9, 0))
                        .build();
                dailyPlan = dailyPlanRepository.save(dailyPlan);
                plan.addDailyPlan(dailyPlan);
                currentDate = currentDate.plusDays(1);
            }
        }

        // DailyPlan의 spots와 travelSegments를 별도로 로드
        List<DailyPlan> dailyPlansWithSpots = dailyPlanRepository.findAllByPlanIdWithSpots(planId);
        List<DailyPlan> dailyPlansWithTravelSegments = dailyPlanRepository.findAllByPlanIdWithTravelSegments(planId);

        // Plan의 dailyPlans를 업데이트
        plan.getDailyPlans().clear();
        plan.getDailyPlans().addAll(dailyPlansWithSpots);

        // travelSegments를 각 DailyPlan에 설정
        for (DailyPlan dailyPlan : plan.getDailyPlans()) {
            dailyPlansWithTravelSegments.stream()
                    .filter(dp -> dp.getId().equals(dailyPlan.getId()))
                    .findFirst()
                    .ifPresent(dp -> dailyPlan.getTravelSegments().addAll(dp.getTravelSegments()));
        }

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

        plan.updatePlan(
                request.getTitle(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTotalBudget(),
                request.getRegion(),
                request.getPrefecture(),
                request.getPrefectureImageUrl()
        );

        return convertToResponse(plan);
    }

    @Transactional
    public void deletePlan(Long planId, Long userId) {
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new PlanNotFoundException(planId));
        
        try {
            // 1. PlanInvitation 삭제 (외래키 제약 조건 때문에 먼저 삭제)
            List<com.ikuzo.tabilog.domain.invitation.PlanInvitation> invitations = planInvitationRepository.findByPlanIdOrderByCreatedAtDesc(planId);
            if (!invitations.isEmpty()) {
                planInvitationRepository.deleteAll(invitations);
            }
            
            // 2. PlanMembers 삭제
            if (!plan.getPlanMembers().isEmpty()) {
                planMemberRepository.deleteAll(plan.getPlanMembers());
            }
            
            // 3. DailyPlans와 연관된 데이터들을 순차적으로 삭제
            if (!plan.getDailyPlans().isEmpty()) {
                for (DailyPlan dailyPlan : plan.getDailyPlans()) {
                    // TravelSegments 먼저 삭제 (spot을 참조하므로)
                    if (!dailyPlan.getTravelSegments().isEmpty()) {
                        travelSegmentRepository.deleteAll(dailyPlan.getTravelSegments());
                    }
                    
                    // Spots 삭제
                    if (!dailyPlan.getSpots().isEmpty()) {
                        spotRepository.deleteAll(dailyPlan.getSpots());
                    }
                    
                    // DailyPlan 삭제
                    dailyPlanRepository.delete(dailyPlan);
                }
            }
            
            // 4. Expenses는 Plan 삭제 시 CASCADE로 자동 삭제됨
            // 5. 마지막에 Plan 삭제
            planRepository.delete(plan);
            
        } catch (Exception e) {
            // 데이터베이스 제약조건 위반 등의 에러 발생 시 로그 출력
            System.err.println("플랜 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("플랜 삭제에 실패했습니다: " + e.getMessage(), e);
        }
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
                .map(dailyPlan -> convertToResponse(dailyPlan, plan.getUser().getId()))
                .collect(Collectors.toList());

        List<PlanMemberResponse> memberResponses = plan.getPlanMembers().stream()
                .map(this::convertToMemberResponse)
                .collect(Collectors.toList());

        // 계획 작성자가 members에 없으면 OWNER로 추가 (기존 데이터 호환성)
        boolean ownerExists = memberResponses.stream()
                .anyMatch(member -> member.getUserId().equals(plan.getUser().getId()));
        
        if (!ownerExists) {
            PlanMemberResponse ownerResponse = PlanMemberResponse.builder()
                    .id(null) // 실제 PlanMember 엔티티가 없는 경우
                    .userId(plan.getUser().getId())
                    .userIdString(plan.getUser().getUserId())
                    .userNickname(plan.getUser().getNickname())
                    .userEmail(plan.getUser().getEmail())
                    .role(PlanMemberRole.OWNER)
                    .build();
            memberResponses.add(0, ownerResponse); // 첫 번째에 추가
        }

        // Expense 데이터 가져오기
        List<ExpenseResponse> expenseResponses = expenseService.getExpensesByPlan(plan.getId());
        
        // 총 지출 금액 계산 (ExpenseService를 통해 정확한 계산)
        Integer expenseTotal = expenseService.getTotalAmountByPlan(plan.getId());
        Long totalExpenseAmount = expenseTotal != null ? expenseTotal.longValue() : 0L;

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
                .expenses(expenseResponses)
                .totalExpenseAmount(totalExpenseAmount)
                .isPublic(plan.isPublic())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private DailyPlanResponse convertToResponse(DailyPlan dailyPlan, Long userId) {
        // 실제 Spot과 TravelSegment 데이터를 가져옴
        List<SpotResponse> spots = spotService.getSpotsByDailyPlan(dailyPlan.getId(), userId);
        List<TravelSegmentResponse> travelSegments = travelSegmentService.getTravelSegmentsByDailyPlan(dailyPlan.getId());
        
        return DailyPlanResponse.builder()
                .id(dailyPlan.getId())
                .visitDate(dailyPlan.getVisitDate())
                .departureTime(dailyPlan.getDepartureTime())
                .spots(spots)
                .travelSegments(travelSegments)
                .createdAt(dailyPlan.getCreatedAt())
                .updatedAt(dailyPlan.getUpdatedAt())
                .build();
    }

    private PlanMemberResponse convertToMemberResponse(PlanMember planMember) {
        return PlanMemberResponse.builder()
                .id(planMember.getId())
                .userId(planMember.getUser().getId())
                .userIdString(planMember.getUser().getUserId())
                .userNickname(planMember.getUser().getNickname())
                .userEmail(planMember.getUser().getEmail())
                .role(planMember.getRole())
                .build();
    }
}
