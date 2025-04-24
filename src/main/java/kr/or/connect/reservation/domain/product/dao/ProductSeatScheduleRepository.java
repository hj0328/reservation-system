package kr.or.connect.reservation.domain.product.dao;

import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import kr.or.connect.reservation.domain.product.dto.ProductSeatScheduleDto;
import kr.or.connect.reservation.domain.product.entity.ProductSeatSchedule;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ProductSeatScheduleRepository extends JpaRepository<ProductSeatSchedule, Long> {

    @Query("SELECT pss.id AS id, pss.eventDateTime AS eventDateTime, pss.reservedQuantity AS reservedQuantity, pss.seatType AS seatType, p.name AS placeName, p.street AS placeStreet, p.tel AS placeTel " +
            "FROM ProductSeatSchedule pss, Place p " +
            "WHERE pss.product.id = :productId AND pss.place.id = p.id AND pss.id > :productSeatScheduleId " +
            "ORDER BY pss.id DESC, pss.eventDateTime DESC ")
    List<ProductSeatScheduleDto> findAllScheduleFromPssId(Long productId, Long productSeatScheduleId, Pageable pageRequest);

    /*
     * 예매 시 동시성 이슈 방지
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductSeatSchedule> findById(Long id);

    @Query("SELECT p.id AS productId, p.title AS title, p.description AS description, " +
            "p.runningTime AS runningTime, p.releaseDate AS releaseDate, p.category.name AS categoryName, " +
            "SUM(pss.reservedQuantity) AS totalReservedCount " +
            "FROM Product p " +
                "JOIN ProductSeatSchedule pss ON pss.product.id = p.id " +
            "GROUP BY p.id " +
            "ORDER BY totalReservedCount DESC, p.releaseDate DESC, p.title")
    List<PopularProductDto> findPagedPopularProduct(PageRequest pageRequest);

    /**
     * in-memory 캐싱하기 위한 전체 조회
     */
    @Query("SELECT p.id as productId, p.title as title, p.description as description, " +
            "p.runningTime as runningTime, p.releaseDate as releaseDate, p.category.name AS categoryName, " +
            "SUM(pss.reservedQuantity) as totalReservedCount " +
            "FROM Product p " +
                "JOIN ProductSeatSchedule pss ON pss.product.id = p.id " +
            "GROUP BY p.id " +
            "ORDER BY totalReservedCount DESC, p.releaseDate DESC")
    List<PopularProductDto> findAllPopularProducts();

    @Query("SELECT p.id AS productId, p.title AS title, p.description AS description, " +
            "p.runningTime as runningTime, p.releaseDate as releaseDate, p.category.name AS categoryName, " +
            " SUM(pss.reservedQuantity) as totalReservedCount " +
            "FROM ProductSeatSchedule pss left join pss.product p " +
            "WHERE p.category.id=:categoryId AND pss.reservedQuantity is not null " +
            "GROUP BY p.id " +
            "ORDER BY totalReservedCount DESC, p.releaseDate DESC")
    List<PopularProductDto> findPopularProductByCategory(PageRequest pageRequest, Long categoryId);
}
