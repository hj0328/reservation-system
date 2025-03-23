package kr.or.connect.reservation.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class NewReservationRequest {
    private Long memberId;
    private Long productId;

    private LocalDate reservedDate;
    private List<ReservationPriceDto> reservationPriceDtos;

    @Builder
    public NewReservationRequest(Long memberId, Long productId, LocalDate reservedDate, List<ReservationPriceDto> reservationPriceDtos) {
        this.memberId = memberId;
        this.productId = productId;
        this.reservedDate = reservedDate;
        this.reservationPriceDtos = reservationPriceDtos;
    }
}
