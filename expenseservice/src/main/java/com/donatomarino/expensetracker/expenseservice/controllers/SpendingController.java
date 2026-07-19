package com.donatomarino.expensetracker.expenseservice.controllers;

import com.donatomarino.expensetracker.expenseservice.dtos.GetSpendingResponse;
import com.donatomarino.expensetracker.expenseservice.services.impl.SpendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SpendingController {

  private final SpendingService spendingService;

  /**
   * Returns the user's spendings for a specific month and year.
   * @param authentication authenticated user
   * @param year year to filter by
   * @param month month to filter by, from 1 to 12
   * @return ResponseEntity containing the user's spending for the specified month and year
   * @throws ResponseStatusException if month is not between 1 and 12
   */
  @GetMapping("/spendings/month")
  public ResponseEntity<GetSpendingResponse> getSpendingsByMonth(Authentication authentication,
                                                                 @RequestParam("year") Integer year,
                                                                 @RequestParam("month") Integer month
  ) {
    if (month < 1 || month > 12) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "month must be between 1 and 12");
    }

    Long userId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(this.spendingService.getSpendingsByUserIdAndMonth(userId, year, month));
  }

  /**
   * Get the user's spendings within a date range.
   * @param authentication authenticated user
   * @param startDate start date of the range
   * @param endDate end date of the range
   * @return ResponseEntity containing the user's spendings within the specified date range
   * @throws ResponseStatusException if startDate is after endDate
   */
  @GetMapping("/spendings/range")
  public ResponseEntity<GetSpendingResponse> getSpendingsByDate(Authentication authentication,
                                                                @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    if (startDate.isAfter(endDate)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date should be after end date");
    }

    Long userId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(this.spendingService.getSpendingsByUserIdAndDateRange(userId, startDate, endDate));
  }
}
