package kr.or.connect.reservation.domain.reservation.dto;

import java.time.LocalDate;

public interface ReservationDetail {
    Long getReservationId();
    LocalDate getReservedDate();
    Long getProductId();
    String getTitle();
    Integer getTotalReservedQuantity();
    Integer getTotalPaid();
}
