package com.ikuzo.tabilog.dto.response;

import com.ikuzo.tabilog.domain.expense.Expense;
import com.ikuzo.tabilog.domain.expense.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    
    private Long id;
    private Long planId;
    private Long spotId;
    private String item;
    private Integer amount;
    private ExpenseCategory category;
    private String categoryDisplayName;
    private LocalDate expenseDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    
    public static ExpenseResponse from(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .planId(expense.getPlan().getId())
                .spotId(expense.getSpot() != null ? expense.getSpot().getId() : null)
                .item(expense.getItem())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .categoryDisplayName(expense.getCategory().getDisplayName())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
