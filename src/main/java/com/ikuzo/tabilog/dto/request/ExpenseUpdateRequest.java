package com.ikuzo.tabilog.dto.request;

import com.ikuzo.tabilog.domain.expense.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseUpdateRequest {
    
    @NotBlank(message = "지출 항목은 필수입니다")
    private String item;
    
    @NotNull(message = "금액은 필수입니다")
    @PositiveOrZero(message = "금액은 0 이상이어야 합니다")
    private Integer amount;
    
    @NotNull(message = "카테고리는 필수입니다")
    private ExpenseCategory category;
    
    @NotNull(message = "지출 날짜는 필수입니다")
    private LocalDate expenseDate;
}
