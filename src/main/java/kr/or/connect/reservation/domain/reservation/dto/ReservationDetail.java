package kr.or.connect.reservation.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReservationDetail {
    Long getReservationId();
    LocalDate getReservedDate();
    Long getProductId();
    String getTitle();
    Integer getTotalReservedQuantity();
    Integer getTotalPaid();
    LocalDateTime getReservationCreatedAt();
}
