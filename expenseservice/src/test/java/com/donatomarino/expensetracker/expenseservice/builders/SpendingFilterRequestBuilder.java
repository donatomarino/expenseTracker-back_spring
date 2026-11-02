package com.donatomarino.expensetracker.expenseservice.builders;

import com.donatomarino.expensetracker.expenseservice.dtos.SpendingFilterRequest;
import com.donatomarino.expensetracker.expenseservice.enums.PaymentMethod;
import com.donatomarino.expensetracker.expenseservice.enums.SpendingSort;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SpendingFilterRequestBuilder {
  private Integer year;
  private Integer month;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal minAmount;
  private BigDecimal maxAmount;
  private String search;
  private SpendingSort sort;
  private PaymentMethod paymentMethod;

  public SpendingFilterRequestBuilder withYear(Integer year) {
    this.year = year;
    return this;
  }

  public SpendingFilterRequestBuilder withMonth(Integer month) {
    this.month = month;
    return this;
  }

  public SpendingFilterRequestBuilder withStartDate(LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

  public SpendingFilterRequestBuilder withEndDate(LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

  public SpendingFilterRequestBuilder withMinAmount(BigDecimal minAmount) {
    this.minAmount = minAmount;
    return this;
  }

  public SpendingFilterRequestBuilder withMaxAmount(BigDecimal maxAmount) {
    this.maxAmount = maxAmount;
    return this;
  }

  public SpendingFilterRequestBuilder withSearch(String search) {
    this.search = search;
    return this;
  }

  public SpendingFilterRequestBuilder withSort(SpendingSort sort) {
    this.sort = sort;
    return this;
  }

  public SpendingFilterRequestBuilder withPaymentMethod(
    PaymentMethod paymentMethod
  ) {
    this.paymentMethod = paymentMethod;
    return this;
  }

  public SpendingFilterRequestBuilder copy() {
    SpendingFilterRequestBuilder copy = new SpendingFilterRequestBuilder();

    copy.year = this.year;
    copy.month = this.month;
    copy.startDate = this.startDate;
    copy.endDate = this.endDate;
    copy.minAmount = this.minAmount;
    copy.maxAmount = this.maxAmount;
    copy.search = this.search;
    copy.sort = this.sort;
    copy.paymentMethod = this.paymentMethod;

    return copy;
  }

  public SpendingFilterRequest build() {
    return new SpendingFilterRequest(
      year,
      month,
      startDate,
      endDate,
      minAmount,
      maxAmount,
      search,
      sort,
      paymentMethod
    );
  }
}
