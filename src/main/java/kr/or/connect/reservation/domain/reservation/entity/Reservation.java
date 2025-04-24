package kr.or.connect.reservation.domain.reservation.entity;

import kr.or.connect.reservation.domain.BaseEntity;
import kr.or.connect.reservation.domain.member.entity.Member;
import kr.or.connect.reservation.domain.product.entity.Product;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @Column(name = "reservation_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "reservation")
    private List<ReservationPrice> reservationPrice = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status")
    private ReservationStatus reservationStatus;

    @Column(name = "reserved_date")
    private LocalDate reservedDate;

    private Reservation(ReservationStatus reservationStatus, LocalDate reservedDate) {
        this.reservationStatus = reservationStatus;
        this.reservedDate = reservedDate;
    }

    public static Reservation create(ReservationStatus reservationStatus, LocalDate reservedDate) {
        return new Reservation(reservationStatus, reservedDate);
    }

    public void setReservationInfo(Member member, Product product) {
        this.member = member;
        this.product = product;
    }

    public void cancel() {
        this.reservationStatus = ReservationStatus.CANCELLED;
    }

    public void watch() {
        this.reservationStatus = ReservationStatus.WATCHED;
    }
}
