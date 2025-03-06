package kr.or.connect.reservation.domain.product.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
public class ProductSeatScheduleRequest {
    @NotNull
    private Long productId;
    @NotNull
    private Long placeId;
    @NotNull
    private LocalDateTime eventDateTime;
    @NotNull
    private String seatType;
}
