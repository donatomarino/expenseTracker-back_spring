package com.donatomarino.expensetracker.expenseservice.controllers;

import com.donatomarino.expensetracker.expenseservice.dtos.*;
import com.donatomarino.expensetracker.expenseservice.enums.PaymentMethod;
import com.donatomarino.expensetracker.expenseservice.services.impl.JwtServiceImpl;
import com.donatomarino.expensetracker.expenseservice.services.interfaces.ISpendingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpendingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SpendingControllerTest {

  private static final Long USER_ID = 1L;
  private static final String URL = "/api/spendings";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ISpendingService spendingService;

  @MockitoBean
  private JwtServiceImpl jwtService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String asJsonString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static UsernamePasswordAuthenticationToken auth() {
    return new UsernamePasswordAuthenticationToken(USER_ID, null, List.of());
  }

  private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, Object body) {
    return builder
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJsonString(body))
      .principal(auth());
  }

  private SpendingResponse prepareGetSpendingResponse() {
    return new SpendingResponse(
      1L,
      "Test Spending",
      new BigDecimal("100.00"),
      PaymentMethod.CREDIT_CARD,
      LocalDate.of(2026, 6, 1)
    );
  }

  private GetSpendingResponse emptyResponse() {
    return new GetSpendingResponse(
      Collections.emptyList(), 1, 10, 0L, 0, true, true,
      new SpendingStatistics(BigDecimal.ZERO, 0L, BigDecimal.ZERO)
    );
  }

  private SpendingRequest spendingRequest(String description, BigDecimal amount, PaymentMethod method, LocalDate date) {
    return new SpendingRequest(description, amount, method, date);
  }

  private SpendingResponse toResponse(Long id, SpendingRequest request) {
    return new SpendingResponse(id, request.description(), request.amount(), request.paymentMethod(), request.date());
  }

  @Test
  void shouldGetSpendingSuccessfully() throws Exception {
    when(spendingService.getSpendingsByUserIdAndFilters(eq(USER_ID), any(), any())).thenReturn(new GetSpendingResponse(
      Collections.singletonList(prepareGetSpendingResponse()),
      1,
      1,
      1,
      1,
      true,
      true,
      new SpendingStatistics(
        new BigDecimal("100.00"),
        1L,
        new BigDecimal("100.00")
      )
    ));

    mockMvc.perform(get(URL).principal(auth()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.spendings[0].id").value(1L))
      .andExpect(jsonPath("$.spendings[0].description").value("Test Spending"))
      .andExpect(jsonPath("$.spendings[0].amount").value(100.00))
      .andExpect(jsonPath("$.spendings[0].paymentMethod").value("CREDIT_CARD"))
      .andExpect(jsonPath("$.spendings[0].date").value("2026-06-01"))
      .andExpect(jsonPath("$.totalElements").value(1))
      .andExpect(jsonPath("$.totalPages").value(1))
      .andExpect(jsonPath("$.page").value(1))
      .andExpect(jsonPath("$.size").value(1))
      .andExpect(jsonPath("$.statistics.totalSpent").value(100.00))
      .andExpect(jsonPath("$.statistics.totalTransactions").value(1))
      .andExpect(jsonPath("$.statistics.average").value(100.00));

    verify(spendingService).getSpendingsByUserIdAndFilters(eq(USER_ID), any(), any());
  }

  @Test
  void shouldGetSpendingsWithFilters() throws Exception {
    when(spendingService.getSpendingsByUserIdAndFilters(eq(USER_ID), any(), any())).thenReturn(emptyResponse());

    mockMvc.perform(get(URL)
        .param("year", "2026")
        .param("month", "10")
        .param("paymentMethod", "CREDIT_CARD")
        .param("minAmount", "10")
        .param("maxAmount", "100")
        .param("search", "test")
        .param("page", "0")
        .param("size", "10")
        .principal(auth())
      )
      .andExpect(status().isOk());

    verify(spendingService).getSpendingsByUserIdAndFilters(
      eq(USER_ID),
      any(SpendingFilterRequest.class),
      any(Pageable.class)
    );
  }

  @Test
  void shouldReturnEmptyListWhenNoSpending() throws Exception {
    when(spendingService.getSpendingsByUserIdAndFilters(eq(USER_ID), any(), any())).thenReturn(emptyResponse());

    mockMvc.perform(get(URL).principal(auth()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.spendings").isEmpty())
      .andExpect(jsonPath("$.totalElements").value(0))
      .andExpect(jsonPath("$.totalPages").value(0))
      .andExpect(jsonPath("$.page").value(1))
      .andExpect(jsonPath("$.size").value(10))
      .andExpect(jsonPath("$.statistics.totalSpent").value("0"))
      .andExpect(jsonPath("$.statistics.totalTransactions").value(0))
      .andExpect(jsonPath("$.statistics.average").value(0.0));

    verify(spendingService).getSpendingsByUserIdAndFilters(eq(USER_ID), any(), any());
  }

  @Test
  void shouldReturn400WhenStartDateIsAfterEndDate() throws Exception {
    when(spendingService.getSpendingsByUserIdAndFilters(any(), any(), any()))
      .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date"));

    mockMvc.perform(get(URL)
        .param("startDate", "2026-06-10")
        .param("endDate", "2026-06-01")
        .principal(auth())
      )
      .andExpect(status().isBadRequest());

    verify(spendingService).getSpendingsByUserIdAndFilters(eq(USER_ID), any(), any());
  }

  @Test
  void shouldAddSpendingSuccessfully() throws Exception {
    SpendingRequest request = spendingRequest(
      "Test Spending", new BigDecimal("100.00"), PaymentMethod.CREDIT_CARD, LocalDate.of(2026, 6, 1));

    when(spendingService.addNewSpending(eq(USER_ID), eq(request))).thenReturn(toResponse(1L, request));

    mockMvc.perform(json(post(URL), request))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(1L))
      .andExpect(jsonPath("$.description").value("Test Spending"))
      .andExpect(jsonPath("$.amount").value(100.00))
      .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
      .andExpect(jsonPath("$.date").value("2026-06-01"));

    verify(spendingService).addNewSpending(eq(USER_ID), eq(request));
  }

  @Test
  void shouldReturn400WhenAddSpendingRequestIsInvalid() throws Exception {
    SpendingRequest request = spendingRequest("", new BigDecimal("-100.00"), null, LocalDate.of(2026, 6, 1));

    mockMvc.perform(json(post(URL), request))
      .andExpect(status().isBadRequest());

    verify(spendingService, never()).addNewSpending(any(), any());
  }

  @Test
  void shouldUpdateSpendingSuccessfully() throws Exception {
    Long spendingId = 1L;
    SpendingRequest request = spendingRequest(
      "Updated Spending", new BigDecimal("150.00"), PaymentMethod.DEBIT_CARD, LocalDate.of(2026, 6, 2));

    when(spendingService.updateSpendingById(eq(USER_ID), eq(spendingId), eq(request)))
      .thenReturn(toResponse(spendingId, request));

    mockMvc.perform(json(put(URL + "/{id}", spendingId), request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(spendingId))
      .andExpect(jsonPath("$.description").value("Updated Spending"))
      .andExpect(jsonPath("$.amount").value(150.00))
      .andExpect(jsonPath("$.paymentMethod").value("DEBIT_CARD"))
      .andExpect(jsonPath("$.date").value("2026-06-02"));

    verify(spendingService).updateSpendingById(eq(USER_ID), eq(spendingId), eq(request));
  }

  @Test
  void shouldReturn404WhenUpdatingNonExistingSpending() throws Exception {
    Long spendingId = 999L;
    SpendingRequest request = spendingRequest(
      "Updated Spending", new BigDecimal("150.00"), PaymentMethod.DEBIT_CARD, LocalDate.of(2026, 6, 2));

    when(spendingService.updateSpendingById(eq(USER_ID), eq(spendingId), eq(request)))
      .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Spending not found"));

    mockMvc.perform(json(put(URL + "/{id}", spendingId), request))
      .andExpect(status().isNotFound());

    verify(spendingService).updateSpendingById(eq(USER_ID), eq(spendingId), eq(request));
  }

  @Test
  void shouldDeleteSpendingSuccessfully() throws Exception {
    Long spendingId = 1L;

    mockMvc.perform(delete(URL + "/{id}", spendingId).principal(auth()))
      .andExpect(status().isNoContent());

    verify(spendingService).deleteSpending(eq(USER_ID), eq(spendingId));
  }
}
