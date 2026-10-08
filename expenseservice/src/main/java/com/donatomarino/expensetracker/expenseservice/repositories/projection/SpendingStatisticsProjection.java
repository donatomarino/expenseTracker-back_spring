package com.donatomarino.expensetracker.expenseservice.repositories.projection;

import java.math.BigDecimal;

public interface SpendingStatisticsProjection {
  BigDecimal getTotalSpent();
  Long getTotalTransactions();
  BigDecimal getAverage();
}
