package com.donatomarino.expensetracker.expenseservice.dtos;

import com.donatomarino.expensetracker.expenseservice.enums.PaymentMethod;
import com.donatomarino.expensetracker.expenseservice.enums.SpendingSort;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpendingFilterRequest(
  @Min(value = 2010, message = "Year must be greater than or equal to 2010")
  @Max(value = 2100, message = "Year must be less than or equal to 2100")
  Integer year,

  @Min(value = 1, message = "Month must be between 1 and 12")
  @Max(value = 12, message = "Month must be between 1 and 12")
  Integer month,

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  LocalDate startDate,

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  LocalDate endDate,

  @DecimalMin(
    value = "0.0",
    message = "Minimum amount cannot be negative"
  )
  BigDecimal minAmount,

  @DecimalMin(
    value = "0.0",
    message = "Maximum amount cannot be negative"
  )
  BigDecimal maxAmount,

  String search,

  SpendingSort sort,

  PaymentMethod paymentMethod
) {
}
