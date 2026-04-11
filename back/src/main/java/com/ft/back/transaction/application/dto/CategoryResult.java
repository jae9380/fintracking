package com.ft.back.transaction.application.dto;

import com.ft.back.transaction.domain.Category;
import com.ft.back.transaction.domain.CategoryType;

public record CategoryResult(Long id, String name, CategoryType type, boolean isDefault) {

    public static CategoryResult from(Category category) {
        return new CategoryResult(
                category.getId(),
                category.getName(),
                category.getType(),
                category.isDefault()
        );
    }
}
