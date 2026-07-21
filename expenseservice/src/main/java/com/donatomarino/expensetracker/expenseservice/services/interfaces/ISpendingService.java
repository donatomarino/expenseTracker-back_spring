package com.donatomarino.expensetracker.expenseservice.services.interfaces;

import com.donatomarino.expensetracker.expenseservice.dtos.*;

import java.time.LocalDate;

public interface ISpendingService {
  GetSpendingResponse getSpendingsByUserIdAndMonth(Long userId, Integer year, Integer month);
  GetSpendingResponse getSpendingsByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
  SpendingResponse addNewSpending(Long userId, CreateSpendingRequest request);
  SpendingResponse updateSpendingById(Long userId, Long spendingId, UpdateSpendingRequest request);
  void deleteSpending(Long userId, Long spendingId);
}
