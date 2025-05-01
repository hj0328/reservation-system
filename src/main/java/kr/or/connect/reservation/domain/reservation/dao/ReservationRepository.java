package kr.or.connect.reservation.domain.reservation.dao;

import kr.or.connect.reservation.domain.reservation.dto.ReservationDetail;
import kr.or.connect.reservation.domain.reservation.entity.Reservation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query(value = "SELECT " +
            "    r.reservation_id as reservationId, r.reserved_date as reservedDate, p.product_id productId, p.title, " +
            "    SUM(rp.reserved_quantity) AS totalReservedQuantity, " +
            "    SUM(rp.reserved_price * rp.reserved_quantity) AS totalPaid," +
            "    r.created_at AS reservationCreatedAt " +
            "FROM reservation r " +
            "JOIN reservation_price rp ON r.reservation_id = rp.reservation_id " +
            "JOIN product p ON r.product_id = p.product_id " +
            "WHERE r.member_id = :memberId " +
            "GROUP BY r.reservation_id, p.product_id, p.title, r.reserved_date " +
            "ORDER BY reservationCreatedAt DESC", nativeQuery = true)
    List<ReservationDetail> findMemberReservation(Long memberId, PageRequest pageRequest);
}
