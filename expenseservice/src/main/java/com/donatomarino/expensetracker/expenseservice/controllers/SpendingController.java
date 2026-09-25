package com.donatomarino.expensetracker.expenseservice.controllers;

import com.donatomarino.expensetracker.expenseservice.dtos.*;
import com.donatomarino.expensetracker.expenseservice.services.interfaces.ISpendingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spendings")
public class SpendingController {

  private final ISpendingService spendingService;
  /**
   * Get the user's spendings within a date range.
   * @param authentication authenticated user
   * @param filters the optional spending filters including date, amount, payment method, search and sort
   * @return ResponseEntity containing the user's spendings within the specified date range
   * @throws ResponseStatusException if startDate is after endDate
   */
  @GetMapping
  public ResponseEntity<GetSpendingResponse> getSpendings(Authentication authentication,
                                                                @ModelAttribute SpendingFilterRequest filters,
                                                                @PageableDefault(
                                                                  page = 0,
                                                                  size = 10
                                                                )
                                                                Pageable pageable
  ) {
    Long userId = getAuthenticatedUserId(authentication);
    GetSpendingResponse response = spendingService.getSpendingsByUserIdAndFilters(userId, filters, pageable);

    return ResponseEntity.ok(response);
  }

  /**
   * Add new spending
   *
   * @param authentication authenticated user
   * @param request        request body containing the new spending data
   * @return ResponseEntity containing the new created spending
   */
  @PostMapping
  public ResponseEntity<SpendingResponse> addSpending(Authentication authentication,
                                                      @Valid @RequestBody CreateSpendingRequest request
  ) {
    Long userId = getAuthenticatedUserId(authentication);
    SpendingResponse response = this.spendingService.addNewSpending(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Updates an existing spending
   *
   * @param authentication authenticated user
   * @param id             spendingId
   * @param request        request body containing the update spending data
   * @return ResponseEntity containing the update spending
   */
  @PutMapping("/{id}")
  public ResponseEntity<SpendingResponse> updateSpending(Authentication authentication,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody UpdateSpendingRequest request
  ) {
    Long userId = getAuthenticatedUserId(authentication);
    SpendingResponse response = this.spendingService.updateSpendingById(userId, id, request);

    return ResponseEntity.ok(response);
  }

  /**
   * Deletes an existing spending.
   *
   * @param authentication authenticated user
   * @param id             spending id
   * @return ResponseEntity with no content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSpending(Authentication authentication,
                                             @PathVariable Long id
  ) {
    Long userId = getAuthenticatedUserId(authentication);
    this.spendingService.deleteSpending(userId, id);
    return ResponseEntity.noContent().build();
  }

  private Long getAuthenticatedUserId(Authentication authentication) {
    return (Long) authentication.getPrincipal();
  }
}
