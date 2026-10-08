package com.donatomarino.expensetracker.expenseservice.dtos;

import com.donatomarino.expensetracker.expenseservice.enums.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSpendingRequest(
  @NotBlank(message = "Description is required")
  @Size(min = 4, max = 100, message = "Description must be at least 4 characters")
  String description,

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be greater than 0")
  @Digits(integer = 10, fraction = 2, message = "Amount must have up to 10 integer digits and 2 decimal places")
  BigDecimal amount,

  @NotNull(message = "Payment method is required")
  PaymentMethod paymentMethod,

  @NotNull(message = "Spending date is required")
  @PastOrPresent(message = "Spending date cannot be in the future")
  LocalDate date
) {
}
