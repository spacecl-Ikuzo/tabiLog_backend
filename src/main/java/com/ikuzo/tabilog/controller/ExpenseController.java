package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.dto.request.ExpenseRequest;
import com.ikuzo.tabilog.dto.request.ExpenseUpdateRequest;
import com.ikuzo.tabilog.dto.response.ExpenseResponse;
import com.ikuzo.tabilog.dto.response.ExpenseSummaryResponse;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@Valid @RequestBody ExpenseRequest request) {
        log.info("=== Expense 생성 요청 시작 ===");
        log.info("요청 시간: {}", java.time.LocalDateTime.now());
        log.info("요청 데이터: {}", request);
        
        try {
            ExpenseResponse response = expenseService.createExpense(request);
            log.info("=== Expense 생성 성공 ===");
            log.info("응답 데이터: {}", response);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("지출이 성공적으로 생성되었습니다.", response));
        } catch (Exception e) {
            log.error("=== Expense 생성 실패 ===");
            log.error("오류 메시지: {}", e.getMessage());
            log.error("오류 스택: ", e);
            throw e;
        }
    }
    
    @PutMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseUpdateRequest request) {
        ExpenseResponse response = expenseService.updateExpense(expenseId, request);
        return ResponseEntity.ok(ApiResponse.success("지출이 성공적으로 수정되었습니다.", response));
    }
    
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.ok(ApiResponse.success("지출이 성공적으로 삭제되었습니다.", null));
    }
    
    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpense(@PathVariable Long expenseId) {
        ExpenseResponse response = expenseService.getExpense(expenseId);
        return ResponseEntity.ok(ApiResponse.success("지출 정보를 성공적으로 조회했습니다.", response));
    }
    
    @GetMapping("/plan/{planId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByPlan(@PathVariable Long planId) {
        List<ExpenseResponse> response = expenseService.getExpensesByPlan(planId);
        return ResponseEntity.ok(ApiResponse.success("플랜별 지출 목록을 성공적으로 조회했습니다.", response));
    }
    
    @GetMapping("/spot/{spotId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesBySpot(@PathVariable Long spotId) {
        List<ExpenseResponse> response = expenseService.getExpensesBySpot(spotId);
        return ResponseEntity.ok(ApiResponse.success("스팟별 지출 목록을 성공적으로 조회했습니다.", response));
    }
    
    @GetMapping("/plan/{planId}/spot/{spotId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByPlanAndSpot(
            @PathVariable Long planId,
            @PathVariable Long spotId) {
        List<ExpenseResponse> response = expenseService.getExpensesByPlanAndSpot(planId, spotId);
        return ResponseEntity.ok(ApiResponse.success("플랜-스팟별 지출 목록을 성공적으로 조회했습니다.", response));
    }
    
    @GetMapping("/plan/{planId}/summary")
    public ResponseEntity<ApiResponse<ExpenseSummaryResponse>> getExpenseSummaryByPlan(@PathVariable Long planId) {
        ExpenseSummaryResponse response = expenseService.getExpenseSummaryByPlan(planId);
        return ResponseEntity.ok(ApiResponse.success("플랜별 지출 요약을 성공적으로 조회했습니다.", response));
    }
    
    @GetMapping("/spot/{spotId}/summary")
    public ResponseEntity<ApiResponse<ExpenseSummaryResponse>> getExpenseSummaryBySpot(@PathVariable Long spotId) {
        ExpenseSummaryResponse response = expenseService.getExpenseSummaryBySpot(spotId);
        return ResponseEntity.ok(ApiResponse.success("스팟별 지출 요약을 성공적으로 조회했습니다.", response));
    }
    
    @GetMapping("/plan/{planId}/total")
    public ResponseEntity<ApiResponse<Integer>> getTotalAmountByPlan(@PathVariable Long planId) {
        Integer totalAmount = expenseService.getTotalAmountByPlan(planId);
        return ResponseEntity.ok(ApiResponse.success("플랜별 총 지출 금액을 성공적으로 조회했습니다.", totalAmount));
    }
    
    @GetMapping("/spot/{spotId}/total")
    public ResponseEntity<ApiResponse<Integer>> getTotalAmountBySpot(@PathVariable Long spotId) {
        Integer totalAmount = expenseService.getTotalAmountBySpot(spotId);
        return ResponseEntity.ok(ApiResponse.success("스팟별 총 지출 금액을 성공적으로 조회했습니다.", totalAmount));
    }
    
    @GetMapping("/plan/{planId}/date/{date}/total")
    public ResponseEntity<ApiResponse<Integer>> getTotalAmountByPlanAndDate(
            @PathVariable Long planId,
            @PathVariable LocalDate date) {
        Integer totalAmount = expenseService.getTotalAmountByPlanAndDate(planId, date);
        return ResponseEntity.ok(ApiResponse.success("플랜-날짜별 총 지출 금액을 성공적으로 조회했습니다.", totalAmount));
    }
}
