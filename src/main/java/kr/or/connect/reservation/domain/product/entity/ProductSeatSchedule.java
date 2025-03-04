package kr.or.connect.reservation.domain.product.entity;

import kr.or.connect.reservation.config.exception.CustomException;
import kr.or.connect.reservation.config.exception.CustomExceptionStatus;
import kr.or.connect.reservation.domain.BaseEntity;
import kr.or.connect.reservation.domain.product.dto.ProductSeatScheduleRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Getter
@Table(name = "product_seat_schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSeatSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_seat_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(name = "event_date_time")
    private LocalDateTime eventDateTime;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "seat_type")
    private SeatType seatType;

    private ProductSeatSchedule(Long id, Product product, Place place,
                                LocalDateTime eventDateTime, Integer reservedQuantity,
                                SeatType seatType) {
        this.id = id;
        this.product = product;
        this.place = place;
        this.eventDateTime = eventDateTime;
        this.reservedQuantity = reservedQuantity;
        this.seatType = seatType;
    }

    public static ProductSeatSchedule from(ProductSeatScheduleRequest request) {
        return ProductSeatSchedule.create(null, null,
                request.getEventDateTime(), 0, request.getSeatType());
    }

    public void registerPlace(Place place) {
        // 단반향 연관관계 때문에 place에서 연관관계 제거하지 않아도 됨
        this.place = place;
    }

    public static ProductSeatSchedule create (Product product, Place place,
                                             LocalDateTime eventDateTime, Integer reservedQuantity,
                                             String seatType) {
        if (Arrays.stream(SeatType.values())
                .noneMatch(v -> v.name().equals(seatType))) {
            throw new CustomException(CustomExceptionStatus.NO_EXIST_SEAT_TYPE);
        }

        return new ProductSeatSchedule(null, product, place, eventDateTime
                , reservedQuantity, SeatType.valueOf(seatType));
    }

    public void update(LocalDateTime updateEventDateTime, Integer reservedQuantity, Place place) {
        this.eventDateTime = updateEventDateTime;
        this.reservedQuantity = reservedQuantity;
        this.place = place;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductSeatSchedule that = (ProductSeatSchedule) o;
        return Objects.equals(eventDateTime, that.eventDateTime) && Objects.equals(reservedQuantity, that.reservedQuantity) && seatType == that.seatType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventDateTime, reservedQuantity, seatType);
    }

    public void addQuantity(Integer quantity) {
        this.reservedQuantity += quantity;
    }

    public void minusQuantity(Integer reservedQuantity) {
        this.reservedQuantity -= reservedQuantity;
    }
}

