package kr.or.connect.reservation.domain.reservation.entity;

import kr.or.connect.reservation.config.exception.CustomException;
import kr.or.connect.reservation.config.exception.CustomExceptionStatus;
import kr.or.connect.reservation.domain.BaseEntity;
import kr.or.connect.reservation.domain.product.entity.ProductSeatSchedule;
import kr.or.connect.reservation.domain.product.entity.SeatType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Arrays;

@Entity
@Getter
@Table(name = "reservation_price")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationPrice extends BaseEntity {
    @Id
    @Column(name = "reservation_price_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_seat_schedule_id")
    private ProductSeatSchedule productSeatSchedule;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity;

    @Column(name = "reserved_price")
    private Integer reservedPrice;

    @Column(name = "reserved_seat_type")
    @Enumerated(value = EnumType.STRING)
    private SeatType reservedSeatType;

    private ReservationPrice(Long id, Reservation reservation, Integer reservedQuantity,
                             Integer reservedPrice, SeatType reservedSeatType) {
        this.id = id;
        this.reservation = reservation;
        this.reservedQuantity = reservedQuantity;
        this.reservedPrice = reservedPrice;
        this.reservedSeatType = reservedSeatType;
    }

    public static ReservationPrice create(Integer reservedQuantity, Integer reservedPrice, String reservedSeatType) {
        if (Arrays.stream(SeatType.values()).noneMatch(v -> v.name().equals(reservedSeatType))) {
            throw new CustomException(CustomExceptionStatus.NO_SEAT_AVAILABLE);
        }

        return new ReservationPrice(null, null,
                reservedQuantity, reservedPrice, SeatType.valueOf(reservedSeatType));
    }

    public ReservationPrice register(Reservation reservation) {
        this.reservation = reservation;
        return this;
    }

    public void schedule(ProductSeatSchedule productSeatSchedule) {
        this.productSeatSchedule = productSeatSchedule;
    }
}
