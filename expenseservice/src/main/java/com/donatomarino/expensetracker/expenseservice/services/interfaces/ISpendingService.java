package com.donatomarino.expensetracker.expenseservice.services.interfaces;

import com.donatomarino.expensetracker.expenseservice.dtos.*;
import org.springframework.data.domain.Pageable;

public interface ISpendingService {
  GetSpendingResponse getSpendingsByUserIdAndFilters(Long userId, SpendingFilterRequest filters, Pageable pageable);
  SpendingResponse addNewSpending(Long userId, CreateSpendingRequest request);
  SpendingResponse updateSpendingById(Long userId, Long spendingId, UpdateSpendingRequest request);
  void deleteSpending(Long userId, Long spendingId);
}
