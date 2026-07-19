package com.donatomarino.expensetracker.expenseservice.dtos;

import java.util.List;

public record GetSpendingResponse (
  List<SpendingResponse> spendings
) {
}
