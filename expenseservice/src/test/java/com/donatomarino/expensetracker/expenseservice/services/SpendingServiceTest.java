package com.donatomarino.expensetracker.expenseservice.services;

import com.donatomarino.expensetracker.expenseservice.builders.SpendingFilterRequestBuilder;
import com.donatomarino.expensetracker.expenseservice.dtos.*;
import com.donatomarino.expensetracker.expenseservice.entities.Spending;
import com.donatomarino.expensetracker.expenseservice.enums.PaymentMethod;
import com.donatomarino.expensetracker.expenseservice.enums.SpendingSort;
import com.donatomarino.expensetracker.expenseservice.repositories.SpendingRepository;
import com.donatomarino.expensetracker.expenseservice.repositories.projection.SpendingStatisticsProjection;
import com.donatomarino.expensetracker.expenseservice.services.impl.SpendingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpendingServiceTest {

  private static final Long USER_ID = 1L;
  private static final LocalDate OCTOBER_START = LocalDate.of(2026, 10, 1);
  private static final LocalDate OCTOBER_END = LocalDate.of(2026, 10, 31);

  @Mock
  private SpendingRepository spendingRepository;

  @InjectMocks
  private SpendingServiceImpl spendingService;

  private void stubRepositoryResponses(Page<SpendingResponse> page, SpendingStatisticsProjection statistics) {
    when(spendingRepository.findByFilters(
      eq(USER_ID),
      eq(OCTOBER_START),
      eq(OCTOBER_END),
      isNull(),
      isNull(),
      isNull(),
      eq(""),
      any(Pageable.class)
    )).thenReturn(page);
    when(spendingRepository.findStatisticsByFilters(
      eq(USER_ID),
      eq(OCTOBER_START),
      eq(OCTOBER_END),
      isNull(),
      isNull(),
      isNull(),
      eq("")
    )).thenReturn(statistics);
  }

  private SpendingStatisticsProjection statistics(BigDecimal totalSpent, BigDecimal average, Long totalTransactions) {
    SpendingStatisticsProjection statistics = mock(SpendingStatisticsProjection.class);
    when(statistics.getTotalSpent()).thenReturn(totalSpent);
    when(statistics.getAverage()).thenReturn(average);
    when(statistics.getTotalTransactions()).thenReturn(totalTransactions);
    return statistics;
  }

  @Test
  void shouldReturnSpendingFilteredByMonthAndYear() {
    // Create a SpendingFilterRequest with year and month
    SpendingFilterRequest request = new SpendingFilterRequestBuilder()
      .withYear(2026)
      .withMonth(10)
      .build();

    // Create a Pageable object for pagination
    Pageable pageable = PageRequest.of(0, 10);

    // Create a SpendingResponse object to be returned by the repository
    SpendingResponse spending1 = new SpendingResponse(
      1L,
      "Test Spending",
      new BigDecimal("100.00"),
      PaymentMethod.CREDIT_CARD,
      LocalDate.of(2026, 10, 10)
    );

    // Create a Page object to be returned by the repository
    Page<SpendingResponse> spendingPage = new PageImpl<>(
      List.of(spending1),
      pageable,
      1
    );

    stubRepositoryResponses(spendingPage, statistics(new BigDecimal("100.00"), new BigDecimal("100.00"), 1L));

    GetSpendingResponse response = spendingService.getSpendingsByUserIdAndFilters(USER_ID, request, pageable);

    assertThat(response.spendings()).hasSize(1);
    assertThat(response.page()).isEqualTo(1);
    assertThat(response.size()).isEqualTo(10);
    assertThat(response.totalElements()).isEqualTo(1);
    assertThat(response.totalPages()).isEqualTo(1);
    assertThat(response.first()).isTrue();
    assertThat(response.last()).isTrue();

    verify(spendingRepository, times(1)).findByFilters(
      eq(USER_ID),
      eq(LocalDate.of(2026, 10, 1)),
      eq(LocalDate.of(2026, 10, 31)),
      isNull(),
      isNull(),
      isNull(),
      eq(""),
      any(Pageable.class)
    );
  }

  @Test
  void shouldThrowBadRequestWhenYearAndMonthAreNotProvidedTogether() {
    SpendingFilterRequest request = new SpendingFilterRequestBuilder()
      .withYear(2026)
      .build();

    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> spendingService.getSpendingsByUserIdAndFilters(USER_ID, request, PageRequest.of(0, 10))
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Year and month must be provided together", exception.getReason());

    verifyNoInteractions(spendingRepository);
  }

  @Test
  void shouldThrowWhenMonthAndYearAndDateRangeAreProvidedTogether() {
    SpendingFilterRequest request = new SpendingFilterRequestBuilder()
      .withYear(2026)
      .withMonth(10)
      .withStartDate(LocalDate.of(2026, 10, 10))
      .withEndDate(LocalDate.of(2026, 10, 20))
      .build();

    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> spendingService.getSpendingsByUserIdAndFilters(USER_ID, request, PageRequest.of(0, 10))
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Use either month/year or date range, not both", exception.getReason());

    verifyNoInteractions(spendingRepository);
  }

  @Test
  void shouldThrowWhenStartDateIsAfterEndDate() {
    SpendingFilterRequest request = new SpendingFilterRequestBuilder()
      .withStartDate(LocalDate.of(2026, 10, 20))
      .withEndDate(LocalDate.of(2026, 10, 10))
      .build();

    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> spendingService.getSpendingsByUserIdAndFilters(USER_ID, request, PageRequest.of(0, 10))
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Start date cannot be after end date", exception.getReason());

    verifyNoInteractions(spendingRepository);
  }

  @ParameterizedTest
  @CsvSource({
    "OLDEST, spendingDate, ASC",
    "NEWEST, spendingDate, DESC",
    "AMOUNT_LOW_TO_HIGH, amount, ASC",
    "AMOUNT_HIGH_TO_LOW, amount, DESC",
    "DESCRIPTION, description, ASC"
  })
  void shouldApplyRequestedSort(SpendingSort sort, String property, Sort.Direction direction) {
    SpendingFilterRequest request = new SpendingFilterRequestBuilder()
      .withYear(2026)
      .withMonth(10)
      .withSort(sort)
      .build();

    Pageable pageable = PageRequest.of(0, 10);

    Page<SpendingResponse> spendingPage =
      new PageImpl<>(
        List.of(),
        pageable,
        0
      );

    stubRepositoryResponses(spendingPage, statistics(BigDecimal.ZERO, BigDecimal.ZERO, 0L));

    spendingService.getSpendingsByUserIdAndFilters(USER_ID, request, pageable);

    Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(direction, property));

    verify(spendingRepository, times(1)).findByFilters(
      eq(USER_ID),
      eq(LocalDate.of(2026, 10, 1)),
      eq(LocalDate.of(2026, 10, 31)),
      isNull(),
      isNull(),
      isNull(),
      eq(""),
      eq(expectedPageable)
    );
  }

  @Test
  void shouldApplyDefaultSortWhenNoSortIsProvided() {
    SpendingFilterRequest request = new SpendingFilterRequestBuilder()
      .withYear(2026)
      .withMonth(10)
      .build();

    Pageable pageable = PageRequest.of(0, 10);

    Page<SpendingResponse> spendingPage =
      new PageImpl<>(
        List.of(),
        pageable,
        0
      );

    stubRepositoryResponses(spendingPage, statistics(BigDecimal.ZERO, BigDecimal.ZERO, 0L));

    spendingService.getSpendingsByUserIdAndFilters(USER_ID, request, pageable);

    Sort expectedSort = Sort.by(Sort.Direction.DESC, "spendingDate");
    Pageable expectedPageable = PageRequest.of(0, 10, expectedSort);

    verify(spendingRepository, times(1)).findByFilters(
      eq(USER_ID),
      eq(LocalDate.of(2026, 10, 1)),
      eq(LocalDate.of(2026, 10, 31)),
      isNull(),
      isNull(),
      isNull(),
      eq(""),
      eq(expectedPageable)
    );
  }

  @Test
  void shouldAddNewSpendingSuccessfully() {
    SpendingRequest request = new SpendingRequest(
      "Test Spending",
      new BigDecimal("100.00"),
      PaymentMethod.CREDIT_CARD,
      LocalDate.of(2026, 10, 10)
    );

    Spending savedSpending = Spending.builder()
      .id(1L)
      .userId(USER_ID)
      .description(request.description())
      .amount(request.amount())
      .paymentMethod(request.paymentMethod())
      .spendingDate(request.date())
      .build();

    when(spendingRepository.save(any(Spending.class))).thenReturn(savedSpending);

    SpendingResponse response = spendingService.addNewSpending(USER_ID, request);

    ArgumentCaptor<Spending> captor = ArgumentCaptor.forClass(Spending.class);
    verify(spendingRepository, times(1)).save(captor.capture());

    Spending saveSpending = captor.getValue();
    assertEquals(USER_ID, savedSpending.getUserId());
    assertEquals(request.description(), response.description());
    assertEquals(request.amount(), response.amount());
    assertEquals(request.paymentMethod(), response.paymentMethod());
    assertEquals(request.date(), response.date());
  }

  @Test
  void shouldUpdateSpendingSuccessfully() {
    Long spendingId = 1L;

    Spending existingSpending = Spending.builder()
      .id(spendingId)
      .userId(USER_ID)
      .description("Old Description")
      .amount(new BigDecimal("50.00"))
      .paymentMethod(PaymentMethod.CASH)
      .spendingDate(LocalDate.of(2026, 10, 5))
      .build();

    SpendingRequest updateRequest = new SpendingRequest(
      "Updated Description",
      new BigDecimal("75.00"),
      PaymentMethod.CREDIT_CARD,
      LocalDate.of(2026, 10, 15)
    );

    when(spendingRepository.findByIdAndUserId(spendingId, USER_ID)).thenReturn(Optional.of(existingSpending));
    when(spendingRepository.save(any(Spending.class))).thenAnswer(invocation -> invocation.getArgument(0));

    SpendingResponse response = spendingService.updateSpendingById(USER_ID, spendingId, updateRequest);

    assertEquals(spendingId, response.id());
    assertEquals(updateRequest.description(), response.description());
    assertEquals(updateRequest.amount(), response.amount());
    assertEquals(updateRequest.paymentMethod(), response.paymentMethod());
    assertEquals(updateRequest.date(), response.date());
  }

  @Test
  void shouldDeleteSpendingSuccessfully() {
    Long spendingId = 1L;

    Spending existingSpending = Spending.builder()
      .id(spendingId)
      .userId(USER_ID)
      .description("Test Spending")
      .amount(new BigDecimal("100.00"))
      .paymentMethod(PaymentMethod.CREDIT_CARD)
      .spendingDate(LocalDate.of(2026, 10, 10))
      .build();

    when(spendingRepository.findByIdAndUserId(spendingId, USER_ID)).thenReturn(Optional.of(existingSpending));

    spendingService.deleteSpending(USER_ID, spendingId);

    verify(spendingRepository, times(1)).delete(existingSpending);
  }

  @Test
  void shouldThrowNotFoundWhenUpdatingOrDeletingNonExistentSpending() {
    Long spendingId = 999L;

    SpendingRequest updateRequest = new SpendingRequest(
      "Updated Description",
      new BigDecimal("75.00"),
      PaymentMethod.CREDIT_CARD,
      LocalDate.of(2026, 10, 15)
    );

    when(spendingRepository.findByIdAndUserId(spendingId, USER_ID)).thenReturn(Optional.empty());

    ResponseStatusException updateException = assertThrows(
      ResponseStatusException.class,
      () -> spendingService.updateSpendingById(USER_ID, spendingId, updateRequest)
    );

    ResponseStatusException deleteException = assertThrows(
      ResponseStatusException.class,
      () -> spendingService.deleteSpending(USER_ID, spendingId)
    );

    for (ResponseStatusException exception : List.of(updateException, deleteException)) {
      assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
      assertEquals("Spending not found for this user", exception.getReason());
    }

    verify(spendingRepository, never()).save(any(Spending.class));
    verify(spendingRepository, never()).delete(any(Spending.class));
  }

}
