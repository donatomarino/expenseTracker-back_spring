package com.donatomarino.expensetracker.expenseservice.services.impl;

import com.donatomarino.expensetracker.expenseservice.dtos.GetSpendingResponse;
import com.donatomarino.expensetracker.expenseservice.dtos.SpendingResponse;
import com.donatomarino.expensetracker.expenseservice.repositories.SpendingRepository;
import com.donatomarino.expensetracker.expenseservice.services.interfaces.ISpendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class SpendingService implements ISpendingService {

  private final SpendingRepository spendingRepository;

  @Override
  public GetSpendingResponse getSpendingsByUserIdAndMonth(Long userId, Integer year, Integer month) {
    YearMonth yearMonth;
    try {
      yearMonth = YearMonth.of(year, month);
    } catch (DateTimeException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid year or month");
    }

    LocalDate startDate = yearMonth.atDay(1);
    LocalDate endDate = yearMonth.atEndOfMonth();
    return getSpendingsByUserIdAndDateRange(userId, startDate, endDate);
  }

  @Override
  public GetSpendingResponse getSpendingsByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
    return new GetSpendingResponse(this.spendingRepository.findByUserIdAndSpendingDateBetween(userId, startDate, endDate)
      .stream()
      .map(s -> new SpendingResponse(
        s.description(),
        s.amount(),
        s.paymentMethod(),
        s.spendingDate()
      ))
      .toList()
    );
  }
}
