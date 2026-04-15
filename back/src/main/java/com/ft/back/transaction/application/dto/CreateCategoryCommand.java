package com.ft.back.transaction.application.dto;

import com.ft.back.transaction.domain.CategoryType;

public record CreateCategoryCommand(String name, CategoryType type) {}
