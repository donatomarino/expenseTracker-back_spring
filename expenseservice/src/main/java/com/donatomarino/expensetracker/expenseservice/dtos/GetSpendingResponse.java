package com.donatomarino.expensetracker.expenseservice.dtos;

import java.util.List;

public record GetSpendingResponse (
  List<SpendingResponse> spendings,
  int page,
  int size,
  long totalElements,
  int totalPages,
  boolean first,
  boolean last,
  SpendingStatistics statistics
) {
}
