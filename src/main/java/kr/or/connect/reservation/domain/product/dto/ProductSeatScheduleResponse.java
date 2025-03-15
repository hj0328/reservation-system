package kr.or.connect.reservation.domain.product.dto;

import kr.or.connect.reservation.domain.product.entity.Place;
import kr.or.connect.reservation.domain.product.entity.ProductSeatSchedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductSeatScheduleResponse {
    private Long productSeatScheduleId;
    private LocalDateTime eventDateTime;
    private Integer reservedQuantity;
    private String seatType;
    private String placeName;
    private String placeStreet;
    private String placeTel;

    @Builder
    public ProductSeatScheduleResponse(Long productSeatScheduleId, LocalDateTime eventDateTime, Integer reservedQuantity, String seatType, String placeName, String placeStreet, String placeTel, Long productId) {
        this.productSeatScheduleId = productSeatScheduleId;
        this.eventDateTime = eventDateTime;
        this.reservedQuantity = reservedQuantity;
        this.seatType = seatType;
        this.placeName = placeName;
        this.placeStreet = placeStreet;
        this.placeTel = placeTel;
    }

    public static ProductSeatScheduleResponse of(ProductSeatSchedule productSeatSchedule, Place place) {
        return ProductSeatScheduleResponse.builder()
                .productSeatScheduleId(productSeatSchedule.getId())
                .eventDateTime(productSeatSchedule.getEventDateTime())
                .reservedQuantity(productSeatSchedule.getReservedQuantity())
                .seatType(productSeatSchedule.getSeatType().name())
                .placeName(place.getName())
                .placeStreet(place.getStreet())
                .placeTel(place.getTel())
                .build();
    }

    public static ProductSeatScheduleResponse of(ProductSeatScheduleDto dto) {
        return ProductSeatScheduleResponse.builder()
                .productSeatScheduleId(dto.getId())
                .eventDateTime(dto.getEventDateTime())
                .reservedQuantity(dto.getReservedQuantity())
                .seatType(dto.getSeatType())
                .placeName(dto.getPlaceName())
                .placeStreet(dto.getPlaceStreet())
                .placeTel(dto.getPlaceTel())
                .build();
    }
}
