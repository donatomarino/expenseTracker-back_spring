package com.donatomarino.expensetracker.expenseservice.services.impl;

import com.donatomarino.expensetracker.expenseservice.dtos.*;
import com.donatomarino.expensetracker.expenseservice.entities.Spending;
import com.donatomarino.expensetracker.expenseservice.repositories.SpendingRepository;
import com.donatomarino.expensetracker.expenseservice.services.interfaces.ISpendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class SpendingServiceImpl implements ISpendingService {

  private final SpendingRepository spendingRepository;

  @Override
  @Transactional(readOnly = true)
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
  @Transactional(readOnly = true)
  public GetSpendingResponse getSpendingsByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
    return new GetSpendingResponse(this.spendingRepository.findByUserIdAndSpendingDateBetween(userId, startDate, endDate)
      .stream()
      .map(s -> new SpendingResponse(
        s.id(),
        s.description(),
        s.amount(),
        s.paymentMethod(),
        s.spendingDate()
      ))
      .toList()
    );
  }

  @Override
  @Transactional
  public SpendingResponse addNewSpending(Long userId, CreateSpendingRequest request) {
    Spending newSpending = Spending.builder()
      .userId(userId)
      .description(request.description())
      .amount(request.amount())
      .paymentMethod(request.paymentMethod())
      .spendingDate(request.spendingDate())
      .build();

    return saveSpending(newSpending);
  }

  @Override
  @Transactional
  public SpendingResponse updateSpendingById(Long userId, Long spendingId, UpdateSpendingRequest request) {
    Spending spending = this.spendingRepository.findByIdAndUserId(spendingId, userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "The expense does not belong to this user"));

    // Update data
    spending.setDescription(request.description());
    spending.setAmount(request.amount());
    spending.setPaymentMethod(request.paymentMethod());
    spending.setSpendingDate(request.spendingDate());

    return saveSpending(spending);
  }

  private SpendingResponse saveSpending(Spending spending) {
    Spending savedSpending = this.spendingRepository.save(spending);

    return new SpendingResponse(
      savedSpending.getId(),
      savedSpending.getDescription(),
      savedSpending.getAmount(),
      savedSpending.getPaymentMethod(),
      savedSpending.getSpendingDate()
    );
  }
}
