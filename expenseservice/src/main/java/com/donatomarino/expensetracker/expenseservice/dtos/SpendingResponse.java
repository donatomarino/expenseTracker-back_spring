package com.donatomarino.expensetracker.expenseservice.dtos;

import com.donatomarino.expensetracker.expenseservice.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpendingResponse(
  String description,
  BigDecimal amount,
  PaymentMethod paymentMethod,
  LocalDate spendingDate
) {
}
