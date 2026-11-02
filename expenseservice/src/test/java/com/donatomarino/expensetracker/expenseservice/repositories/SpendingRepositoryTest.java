package com.donatomarino.expensetracker.expenseservice.repositories;

import com.donatomarino.expensetracker.expenseservice.dtos.SpendingResponse;
import com.donatomarino.expensetracker.expenseservice.entities.Spending;
import com.donatomarino.expensetracker.expenseservice.enums.PaymentMethod;
import com.donatomarino.expensetracker.expenseservice.repositories.projection.SpendingStatisticsProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class SpendingRepositoryTest {

  private static final LocalDate START = LocalDate.of(2026, 11, 1);
  private static final LocalDate END = LocalDate.of(2026, 11, 30);

  @Autowired
  private SpendingRepository spendingRepository;

  private Long savedId;

  @BeforeEach
  void setUp() {
    Spending spending1 = Spending.builder()
      .userId(1L)
      .description("Test Spending")
      .amount(new BigDecimal("100.00"))
      .paymentMethod(PaymentMethod.CREDIT_CARD)
      .spendingDate(LocalDate.of(2026, 11, 15))
      .build();

    Spending spending2 = Spending.builder()
      .userId(1L)
      .description("Another Spending")
      .amount(new BigDecimal("50.00"))
      .paymentMethod(PaymentMethod.CASH)
      .spendingDate(LocalDate.of(2026, 11, 20))
      .build();

    savedId = spendingRepository.saveAll(List.of(spending1, spending2)).get(0).getId();
  }

  private Page<SpendingResponse> find(Long userId, LocalDate start, LocalDate end, PaymentMethod method, BigDecimal min, BigDecimal max, String search, int size) {
    return spendingRepository.findByFilters(userId, start, end, method, min, max, search,
      PageRequest.of(0, size));
  }

  @Test
  void shouldFindSpendingByFilters() {
    Page<SpendingResponse> result = find(1L, START, END, null, null, null, "", 10);

    assertTrue(result.hasContent());
    assertEquals(2, result.getTotalElements());
  }

  @Test
  void shouldFindSpendingsByPaymentMethod() {
    Page<SpendingResponse> result = find(1L, START, END, PaymentMethod.CASH, null, null, "", 10);

    assertEquals(1, result.getTotalElements());
    assertEquals("Another Spending", result.getContent().get(0).description());
  }

  @Test
  void shouldFindSpendingsBySearch() {
    Page<SpendingResponse> result = find(1L, START, END, null, null, null, "another", 10);

    assertEquals(1, result.getTotalElements());
  }

  @Test
  void shouldFindStatisticsByFilters() {
    SpendingStatisticsProjection statistics = spendingRepository.findStatisticsByFilters(
      1L, START, END,
      null, null, null, ""
    );

    assertEquals(0, new BigDecimal("150.00").compareTo(statistics.getTotalSpent()));
    assertEquals(2L, statistics.getTotalTransactions());
    assertEquals(0, new BigDecimal("75.00").compareTo(statistics.getAverage()));
  }

  @Test
  void shouldFindSpendingByIdAndUserId() {
    Optional<Spending> spending = spendingRepository.findByIdAndUserId(savedId, 1L);

    assertTrue(spending.isPresent());
    assertEquals("Test Spending", spending.get().getDescription());
  }

  @Test
  void shouldFindSpendingByIdAndUserIdNotFound() {
    Optional<Spending> spending = spendingRepository.findByIdAndUserId(999L, 1L);

    assertTrue(spending.isEmpty());
  }

  @Test
  void shouldFindSpendingByIdAndUserIdWithDifferentUser() {
    Optional<Spending> spending = spendingRepository.findByIdAndUserId(savedId, 2L);

    assertTrue(spending.isEmpty());
  }
}
