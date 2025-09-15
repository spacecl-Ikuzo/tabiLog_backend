package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.expense.Expense;
import com.ikuzo.tabilog.domain.expense.ExpenseCategory;
import com.ikuzo.tabilog.domain.expense.ExpenseRepository;
import com.ikuzo.tabilog.domain.plan.Plan;
import com.ikuzo.tabilog.domain.plan.PlanRepository;
import com.ikuzo.tabilog.domain.spot.Spot;
import com.ikuzo.tabilog.domain.spot.SpotRepository;
import com.ikuzo.tabilog.dto.request.ExpenseRequest;
import com.ikuzo.tabilog.dto.request.ExpenseUpdateRequest;
import com.ikuzo.tabilog.dto.response.ExpenseResponse;
import com.ikuzo.tabilog.dto.response.ExpenseSummaryResponse;
import com.ikuzo.tabilog.exception.PlanNotFoundException;
import com.ikuzo.tabilog.exception.SpotNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    private final PlanRepository planRepository;
    private final SpotRepository spotRepository;
    
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("플랜을 찾을 수 없습니다: " + request.getPlanId()));
        
        Spot spot = null;
        if (request.getSpotId() != null) {
            spot = spotRepository.findById(request.getSpotId())
                    .orElseThrow(() -> new SpotNotFoundException("스팟을 찾을 수 없습니다: " + request.getSpotId()));
        }
        
        Expense expense = Expense.builder()
                .plan(plan)
                .spot(spot)
                .item(request.getItem())
                .amount(request.getAmount())
                .category(request.getCategory())
                .expenseDate(request.getExpenseDate())
                .build();
        
        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.from(savedExpense);
    }
    
    @Transactional
    public ExpenseResponse updateExpense(Long expenseId, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("지출을 찾을 수 없습니다: " + expenseId));
        
        expense.setItem(request.getItem());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setExpenseDate(request.getExpenseDate());
        
        Expense updatedExpense = expenseRepository.save(expense);
        return ExpenseResponse.from(updatedExpense);
    }
    
    @Transactional
    public void deleteExpense(Long expenseId) {
        if (!expenseRepository.existsById(expenseId)) {
            throw new RuntimeException("지출을 찾을 수 없습니다: " + expenseId);
        }
        expenseRepository.deleteById(expenseId);
    }
    
    public ExpenseResponse getExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("지출을 찾을 수 없습니다: " + expenseId));
        return ExpenseResponse.from(expense);
    }
    
    public List<ExpenseResponse> getExpensesByPlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("플랜을 찾을 수 없습니다: " + planId));
        
        return expenseRepository.findByPlanOrderByExpenseDateAsc(plan)
                .stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }
    
    public List<ExpenseResponse> getExpensesBySpot(Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException("스팟을 찾을 수 없습니다: " + spotId));
        
        return expenseRepository.findBySpotOrderByExpenseDateAsc(spot)
                .stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }
    
    public List<ExpenseResponse> getExpensesByPlanAndSpot(Long planId, Long spotId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("플랜을 찾을 수 없습니다: " + planId));
        
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException("스팟을 찾을 수 없습니다: " + spotId));
        
        return expenseRepository.findByPlanAndSpotOrderByExpenseDateAsc(plan, spot)
                .stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }
    
    public ExpenseSummaryResponse getExpenseSummaryByPlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("플랜을 찾을 수 없습니다: " + planId));
        
        Integer totalAmount = expenseRepository.getTotalAmountByPlan(plan);
        List<Object[]> categoryAmounts = expenseRepository.getAmountByCategoryAndPlan(plan);
        List<ExpenseResponse> expenses = getExpensesByPlan(planId);
        
        Map<ExpenseCategory, Integer> amountByCategory = categoryAmounts.stream()
                .collect(Collectors.toMap(
                        row -> (ExpenseCategory) row[0],
                        row -> (Integer) row[1]
                ));
        
        return ExpenseSummaryResponse.builder()
                .planId(planId)
                .totalAmount(totalAmount)
                .amountByCategory(amountByCategory)
                .expenses(expenses)
                .build();
    }
    
    public ExpenseSummaryResponse getExpenseSummaryBySpot(Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException("스팟을 찾을 수 없습니다: " + spotId));
        
        Integer totalAmount = expenseRepository.getTotalAmountBySpot(spot);
        List<Object[]> categoryAmounts = expenseRepository.getAmountByCategoryAndSpot(spot);
        List<ExpenseResponse> expenses = getExpensesBySpot(spotId);
        
        Map<ExpenseCategory, Integer> amountByCategory = categoryAmounts.stream()
                .collect(Collectors.toMap(
                        row -> (ExpenseCategory) row[0],
                        row -> (Integer) row[1]
                ));
        
        return ExpenseSummaryResponse.builder()
                .spotId(spotId)
                .totalAmount(totalAmount)
                .amountByCategory(amountByCategory)
                .expenses(expenses)
                .build();
    }
    
    public Integer getTotalAmountByPlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("플랜을 찾을 수 없습니다: " + planId));
        
        return expenseRepository.getTotalAmountByPlan(plan);
    }
    
    public Integer getTotalAmountBySpot(Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException("스팟을 찾을 수 없습니다: " + spotId));
        
        return expenseRepository.getTotalAmountBySpot(spot);
    }
    
    public Integer getTotalAmountByPlanAndDate(Long planId, LocalDate date) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("플랜을 찾을 수 없습니다: " + planId));
        
        return expenseRepository.getTotalAmountByPlanAndDate(plan, date);
    }
}
