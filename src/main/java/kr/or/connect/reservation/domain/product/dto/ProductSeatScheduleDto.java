package kr.or.connect.reservation.domain.product.dto;

import java.time.LocalDateTime;

public interface ProductSeatScheduleDto {
    Long getId();
    LocalDateTime getEventDateTime();
    Integer getReservedQuantity();
    String getSeatType();
    String getPlaceName();
    String getPlaceStreet();
    String getPlaceTel();
}
