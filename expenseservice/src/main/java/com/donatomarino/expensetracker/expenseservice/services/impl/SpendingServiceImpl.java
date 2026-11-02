package com.donatomarino.expensetracker.expenseservice.services.impl;

import com.donatomarino.expensetracker.expenseservice.dtos.*;
import com.donatomarino.expensetracker.expenseservice.entities.Spending;
import com.donatomarino.expensetracker.expenseservice.enums.PaymentMethod;
import com.donatomarino.expensetracker.expenseservice.enums.SpendingSort;
import com.donatomarino.expensetracker.expenseservice.repositories.SpendingRepository;
import com.donatomarino.expensetracker.expenseservice.repositories.projection.SpendingStatisticsProjection;
import com.donatomarino.expensetracker.expenseservice.services.interfaces.ISpendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static com.donatomarino.expensetracker.expenseservice.enums.SpendingSort.NEWEST;

@Service
@RequiredArgsConstructor
public class SpendingServiceImpl implements ISpendingService {

  private final SpendingRepository spendingRepository;

  @Transactional(readOnly = true)
  @Override
  public GetSpendingResponse getSpendingsByUserIdAndFilters(Long userId, SpendingFilterRequest filters, Pageable pageable) {
    // Detect whether the request uses the month/year filter
    boolean hasMonthAndYearFilter = filters.year() != null && filters.month() != null;
    // Validate and detect whether the request uses a date range
    boolean hasDateRangeFilter = isHasRangeFilter(filters, hasMonthAndYearFilter);

    // Validate that request doesn't use both filters
    if (hasMonthAndYearFilter && hasDateRangeFilter) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use either month/year or date range, not both");
    }

    // Read the filters
    LocalDate startDate = filters.startDate();
    LocalDate endDate = filters.endDate();

    if (hasMonthAndYearFilter) {
      // Convert the selected year and month
      YearMonth selectedMonth = YearMonth.of(filters.year(), filters.month());

      startDate = selectedMonth.atDay(1);
      endDate = selectedMonth.atEndOfMonth();
    } else if (hasDateRangeFilter) {
      // Validate that the start date is not after the end date.
      if (startDate.isAfter(endDate)) {
        throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Start date cannot be after end date"
        );
      }
    } else {
      // If no date filter is provided, use the current month
      YearMonth currentMonth = YearMonth.now();

      startDate = currentMonth.atDay(1);
      endDate = currentMonth.atEndOfMonth();
    }

    // Read the other optional filters
    PaymentMethod paymentMethod = filters.paymentMethod();
    BigDecimal minAmount = filters.minAmount();
    BigDecimal maxAmount = filters.maxAmount();
    SpendingSort sort = filters.sort();
    String search = filters.search() == null ? "" : filters.search().trim();

    // Validate that the minimum amount is not greater than the maximum amount.
    if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum amount cannot be greater than maximum amount");
    }

    // Convert the requested sort into Spring's Sort
    SpendingSort requestedSort = sort != null ? filters.sort() : NEWEST;
    Sort sorted = switch (requestedSort) {
      case OLDEST -> Sort.by(Sort.Direction.ASC, "spendingDate");

      case NEWEST -> Sort.by(Sort.Direction.DESC, "spendingDate");

      case AMOUNT_LOW_TO_HIGH -> Sort.by(Sort.Direction.ASC, "amount");

      case AMOUNT_HIGH_TO_LOW -> Sort.by(Sort.Direction.DESC, "amount");

      case DESCRIPTION -> Sort.by(Sort.Direction.ASC, "description");
    };

    Pageable sortedPageable = PageRequest.of(
      pageable.getPageNumber(),
      pageable.getPageSize(),
      sorted
    );

    Page<SpendingResponse> spending = this.spendingRepository.findByFilters(
      userId,
      startDate,
      endDate,
      paymentMethod,
      minAmount,
      maxAmount,
      search,
      sortedPageable
    );

    SpendingStatisticsProjection statistics = spendingRepository.findStatisticsByFilters(
      userId,
      startDate,
      endDate,
      paymentMethod,
      minAmount,
      maxAmount,
      search
    );

    SpendingStatistics spendingStatistics = new SpendingStatistics(
      statistics.getTotalSpent(),
      statistics.getTotalTransactions(),
      statistics.getAverage()
    );

    return new GetSpendingResponse(
      spending.getContent(),
      spending.getNumber() + 1,
      spending.getSize(),
      spending.getTotalElements(),
      spending.getTotalPages(),
      spending.isFirst(),
      spending.isLast(),
      spendingStatistics
    );
  }

  private static boolean isHasRangeFilter(SpendingFilterRequest filters, boolean hasMonthAndYearFilter) {
    boolean hasAnyMonthValue = filters.year() != null || filters.month() != null;
    boolean hasDateRangeFilter = filters.startDate() != null && filters.endDate() != null;
    boolean hasAnyDateRangeFilter = filters.startDate() != null || filters.endDate() != null;

    if (hasAnyMonthValue && !hasMonthAndYearFilter) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year and month must be provided together");
    }

    if (hasAnyDateRangeFilter && !hasDateRangeFilter) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end date must be provided together");
    }
    return hasDateRangeFilter;
  }

  @Override
  @Transactional
  public SpendingResponse addNewSpending(Long userId, SpendingRequest request) {
    Spending newSpending = Spending.builder()
      .userId(userId)
      .description(request.description())
      .amount(request.amount())
      .paymentMethod(request.paymentMethod())
      .spendingDate(request.date())
      .build();

    return saveSpending(newSpending);
  }

  @Override
  @Transactional
  public SpendingResponse updateSpendingById(Long userId, Long spendingId, SpendingRequest request) {
    Spending spending = findSpendingByIdAndUser(spendingId, userId);

    // Update data
    spending.setDescription(request.description());
    spending.setAmount(request.amount());
    spending.setPaymentMethod(request.paymentMethod());
    spending.setSpendingDate(request.date());

    return saveSpending(spending);
  }

  @Override
  @Transactional
  public void deleteSpending(Long userId, Long spendingId) {
    Spending spending = findSpendingByIdAndUser(spendingId, userId);
    this.spendingRepository.delete(spending);
  }

  private SpendingResponse saveSpending(Spending spending) {
    Spending savedSpending = this.spendingRepository.save(spending);

    return new SpendingResponse(savedSpending.getId(), savedSpending.getDescription(), savedSpending.getAmount(), savedSpending.getPaymentMethod(), savedSpending.getSpendingDate());
  }

  private Spending findSpendingByIdAndUser(Long spendingId, Long userId) {
    return this.spendingRepository.findByIdAndUserId(spendingId, userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spending not found for this user"));
  }
}
