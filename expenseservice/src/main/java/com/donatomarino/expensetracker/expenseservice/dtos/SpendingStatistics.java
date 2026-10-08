package com.donatomarino.expensetracker.expenseservice.dtos;

import java.math.BigDecimal;

public record SpendingStatistics(
  BigDecimal totalSpent,
  long totalTransactions,
  BigDecimal average
) {
}
