package com.donatomarino.expensetracker.expenseservice.dtos;

import java.util.List;

public record GetSpendingResponse (
  List<SpendingResponse> spendings,
  int page,
  int size,
  long totalElements,
  int totalPage,
  boolean first,
  boolean last
) {
}
