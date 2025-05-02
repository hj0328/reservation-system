package kr.or.connect.reservation.domain.reservation.dao;

import kr.or.connect.reservation.domain.reservation.dto.ReservationDetail;
import kr.or.connect.reservation.domain.reservation.entity.Reservation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query(value = "SELECT *\n" +
            "FROM (\n" +
            "  SELECT \n" +
            "      r.reservation_id AS reservationId, " +
            "      r.reserved_date AS reservedDate, " +
            "      p.product_id as productId, " +
            "      p.title, " +
            "      SUM(rp.reserved_quantity) AS totalReservedQuantity, " +
            "      SUM(rp.reserved_price * rp.reserved_quantity) AS totalPaid " +
            "  FROM reservation r " +
            "  JOIN reservation_price rp ON r.reservation_id = rp.reservation_id " +
            "  JOIN product p ON r.product_id = p.product_id " +
            "  WHERE r.member_id = :memberId " +
            "  GROUP BY r.reservation_id " +
            ") t " +
            "ORDER BY t.reservedDate DESC, t.reservationId DESC ", nativeQuery = true)
    List<ReservationDetail> findMemberReservation(Long memberId, PageRequest pageRequest);
}
