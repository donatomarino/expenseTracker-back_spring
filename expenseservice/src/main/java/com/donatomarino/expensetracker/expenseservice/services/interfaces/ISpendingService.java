package com.donatomarino.expensetracker.expenseservice.services.interfaces;

import com.donatomarino.expensetracker.expenseservice.dtos.GetSpendingResponse;

import java.time.LocalDate;

public interface ISpendingService {
  GetSpendingResponse getSpendingsByUserIdAndMonth(Long userId, Integer year, Integer month);
  GetSpendingResponse getSpendingsByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}
