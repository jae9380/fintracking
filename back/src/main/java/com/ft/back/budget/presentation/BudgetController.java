package com.ft.back.budget.presentation;

import com.ft.back.budget.application.BudgetService;
import com.ft.back.budget.domain.AlertType;
import com.ft.back.budget.presentation.dto.BudgetResponse;
import com.ft.back.budget.presentation.dto.CreateBudgetRequest;
import com.ft.back.budget.presentation.dto.UpdateBudgetRequest;
import com.ft.back.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ApiResponse<BudgetResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateBudgetRequest request) {
        return ApiResponse.created(BudgetResponse.from(budgetService.create(userId, request.toCommand())));
    }

    @GetMapping
    public ApiResponse<List<BudgetResponse>> findAll(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String yearMonth) {
        YearMonth ym = (yearMonth != null) ? YearMonth.parse(yearMonth) : YearMonth.now();
        List<BudgetResponse> responses = budgetService.findAll(userId, ym)
                .stream()
                .map(BudgetResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{budgetId}")
    public ApiResponse<BudgetResponse> findById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long budgetId) {
        return ApiResponse.success(BudgetResponse.from(budgetService.findById(userId, budgetId)));
    }

    @PutMapping("/{budgetId}")
    public ApiResponse<BudgetResponse> updateAmount(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long budgetId,
            @Valid @RequestBody UpdateBudgetRequest request) {
        return ApiResponse.success(BudgetResponse.from(budgetService.updateAmount(userId, budgetId, request.amount())));
    }

    @DeleteMapping("/{budgetId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long budgetId) {
        budgetService.delete(userId, budgetId);
        return ApiResponse.noContent();
    }

    @PostMapping("/{budgetId}/alerts")
    public ApiResponse<List<AlertType>> checkAlerts(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long budgetId) {
        return ApiResponse.success(budgetService.checkAlerts(userId, budgetId));
    }
}
