package com.example.smartstore.controller;

import com.example.smartstore.domain.User;
import com.example.smartstore.dto.ApplyPromotionRequest;
import com.example.smartstore.dto.CartResponse;
import com.example.smartstore.dto.PromotionRequest;
import com.example.smartstore.dto.PromotionResponse;
import com.example.smartstore.service.PromotionEvaluation;
import com.example.smartstore.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Promotions", description = "Smart promotions and cart discounts")
@RestController
@RequestMapping("/promotions")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creates a promotion rule")
    public PromotionResponse create(@RequestBody PromotionRequest request) {
        return promotionService.createPromotion(request);
    }

    @GetMapping
    @Operation(summary = "Lists active promotion rules")
    public List<PromotionResponse> activePromotions() {
        return promotionService.getActivePromotions();
    }

    @GetMapping("/my-promotions")
    @Operation(summary = "Lists promotions eligible for the authenticated user's cart")
    public List<PromotionResponse> myPromotions(@AuthenticationPrincipal User user) {
        return promotionService.getEligiblePromotions(user);
    }

    @GetMapping("/cart")
    @Operation(summary = "Evaluates promotions against the authenticated user's cart")
    public PromotionEvaluation cartPromotions(@AuthenticationPrincipal User user) {
        return promotionService.evaluateCart(user);
    }

    @PostMapping("/apply")
    @Operation(summary = "Applies an eligible promotion to the authenticated user's cart")
    public CartResponse apply(@AuthenticationPrincipal User user, @RequestBody ApplyPromotionRequest request) {
        return promotionService.applyPromotion(user, request.getPromotionId());
    }

    @DeleteMapping("/apply")
    @Operation(summary = "Removes the currently applied promotion from the authenticated user's cart")
    public CartResponse removeApplied(@AuthenticationPrincipal User user) {
        return promotionService.removeAppliedPromotion(user);
    }
}
