package com.donatomarino.expensetracker.expenseservice.repositories;

import com.donatomarino.expensetracker.expenseservice.dtos.SpendingResponse;
import com.donatomarino.expensetracker.expenseservice.entities.Spending;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpendingRepository extends JpaRepository<Spending, Long> {
  List<SpendingResponse> findByUserIdAndSpendingDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
