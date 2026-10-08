package com.donatomarino.expensetracker.expenseservice.repositories;

import com.donatomarino.expensetracker.expenseservice.dtos.SpendingResponse;
import com.donatomarino.expensetracker.expenseservice.entities.Spending;
import com.donatomarino.expensetracker.expenseservice.enums.PaymentMethod;
import com.donatomarino.expensetracker.expenseservice.repositories.projection.SpendingStatisticsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface SpendingRepository extends JpaRepository<Spending, Long> {

  @Query("""
    SELECT new com.donatomarino.expensetracker.expenseservice.dtos.SpendingResponse(
            s.id,
            s.description,
            s.amount,
            s.paymentMethod,
            s.spendingDate
          )
            FROM Spending s
            WHERE s.userId = :userId
            AND s.spendingDate BETWEEN :startDate AND :endDate
            AND (:paymentMethod IS NULL or s.paymentMethod = :paymentMethod)
            AND (:minAmount IS NULL or s.amount >= :minAmount)
            AND (:maxAmount IS NULL or s.amount <= :maxAmount)
            AND (:search = '' OR LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
  Page<SpendingResponse> findByFilters(
    @Param("userId") Long userId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate,
    @Param("paymentMethod") PaymentMethod paymentMethod,
    @Param("minAmount") BigDecimal minAmount,
    @Param("maxAmount") BigDecimal maxAmount,
    @Param("search") String search,
    Pageable pageable
  );

  @Query("""
    SELECT 
        COALESCE(SUM(s.amount)) as totalSpent,
        COUNT(s) AS totalTransactions,
        COALESCE(AVG(s.amount)) as average
    FROM Spending s
    WHERE s.userId = :userId
        AND s.spendingDate BETWEEN :startDate AND :endDate
        AND (:paymentMethod IS NULL or s.paymentMethod = :paymentMethod)
        AND (:minAmount IS NULL or s.amount >= :minAmount)
        AND (:maxAmount IS NULL or s.amount <= :maxAmount)
        AND (:search = '' OR LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
  SpendingStatisticsProjection findStatisticsByFilters(
    @Param("userId") Long userId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate,
    @Param("paymentMethod") PaymentMethod paymentMethod,
    @Param("minAmount") BigDecimal minAmount,
    @Param("maxAmount") BigDecimal maxAmount,
    @Param("search") String search
  );

  Optional<Spending> findByIdAndUserId(Long spendingId, Long userId);
}
