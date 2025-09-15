package com.ikuzo.tabilog.domain.expense;

public enum ExpenseCategory {
    LODGING("宿泊"),
    AVIATION("航空"),
    TRANSPORT("交通"),
    FOOD("食費"),
    SHOPPING("買い物"),
    SIGHTSEEING("観光"),
    OTHER("その他");
    
    private final String displayName;
    
    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
