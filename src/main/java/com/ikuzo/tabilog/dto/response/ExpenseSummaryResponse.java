package com.ikuzo.tabilog.dto.response;

import com.ikuzo.tabilog.domain.expense.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryResponse {
    
    private Long planId;
    private Long spotId;
    private Integer totalAmount;
    private Map<ExpenseCategory, Integer> amountByCategory;
    private List<ExpenseResponse> expenses;
    private LocalDate summaryDate;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryAmount {
        private ExpenseCategory category;
        private String categoryDisplayName;
        private Integer amount;
    }
}
