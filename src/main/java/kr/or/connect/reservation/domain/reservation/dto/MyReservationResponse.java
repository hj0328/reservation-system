package kr.or.connect.reservation.domain.reservation.dto;

import kr.or.connect.reservation.domain.product.entity.Product;
import kr.or.connect.reservation.domain.reservation.entity.Reservation;
import kr.or.connect.reservation.domain.reservation.entity.ReservationPrice;
import kr.or.connect.reservation.domain.reservation.entity.ReservationStatus;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class MyReservationResponse {

    private Long productId;
    private String title;
    private ReservationStatus reservationStatus;
    private List<MyReservationPrice> reservationPriceList;

    public MyReservationResponse(Long productId, String title,
                                 ReservationStatus reservationStatus,
                                 List<MyReservationPrice> reservationPriceList) {
        this.productId = productId;
        this.title = title;
        this.reservationStatus = reservationStatus;
        this.reservationPriceList = reservationPriceList;
    }

    public static MyReservationResponse of(Product product, Reservation reservation,
                                           List<ReservationPrice> reservationPriceList) {

        List<MyReservationPrice> myReservationPrices =
                reservationPriceList.stream()
                        .map(MyReservationPrice::of)
                        .collect(Collectors.toList());

        return new MyReservationResponse(product.getId(),
                product.getTitle(),
                reservation.getReservationStatus(),
                myReservationPrices);
    }
}
