package com.ikuzo.tabilog.domain.expense;

import com.ikuzo.tabilog.domain.plan.Plan;
import com.ikuzo.tabilog.domain.spot.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    // 특정 플랜의 모든 지출 조회
    List<Expense> findByPlanOrderByExpenseDateAsc(Plan plan);
    
    // 특정 스팟의 모든 지출 조회
    List<Expense> findBySpotOrderByExpenseDateAsc(Spot spot);
    
    // 특정 플랜과 스팟의 지출 조회
    List<Expense> findByPlanAndSpotOrderByExpenseDateAsc(Plan plan, Spot spot);
    
    // 특정 플랜의 특정 날짜 지출 조회
    List<Expense> findByPlanAndExpenseDateOrderByExpenseDateAsc(Plan plan, LocalDate expenseDate);
    
    // 특정 플랜의 총 지출 금액 계산
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.plan = :plan")
    Integer getTotalAmountByPlan(@Param("plan") Plan plan);
    
    // 특정 스팟의 총 지출 금액 계산
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.spot = :spot")
    Integer getTotalAmountBySpot(@Param("spot") Spot spot);
    
    // 특정 플랜의 카테고리별 지출 금액 계산
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.plan = :plan GROUP BY e.category")
    List<Object[]> getAmountByCategoryAndPlan(@Param("plan") Plan plan);
    
    // 특정 스팟의 카테고리별 지출 금액 계산
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.spot = :spot GROUP BY e.category")
    List<Object[]> getAmountByCategoryAndSpot(@Param("spot") Spot spot);
    
    // 특정 플랜의 특정 날짜 총 지출 금액 계산
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.plan = :plan AND e.expenseDate = :expenseDate")
    Integer getTotalAmountByPlanAndDate(@Param("plan") Plan plan, @Param("expenseDate") LocalDate expenseDate);
}
