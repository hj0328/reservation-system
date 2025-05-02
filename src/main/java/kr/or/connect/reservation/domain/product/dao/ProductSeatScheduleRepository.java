package kr.or.connect.reservation.domain.product.dao;

import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import kr.or.connect.reservation.domain.product.dao.dto.ProductProfitDto;
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


    @Query("SELECT pss.id AS id, pss.eventDateTime AS eventDateTime, pss.reservedQuantity AS reservedQuantity, pss.seatType AS seatType, p.name AS placeName, p.street AS placeStreet, p.tel AS placeTel " +
            "FROM ProductSeatSchedule pss, Place p " +
            "WHERE pss.product.id = :productId AND pss.place.id = p.id  " +
            "ORDER BY pss.id DESC, pss.eventDateTime DESC ")
    List<ProductSeatScheduleDto> findAllScheduleFromPssIdTemp(Long productId, Pageable pageRequest);

    /*
     * 예매 시 동시성 이슈 방지
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductSeatSchedule> findById(Long id);

//    @Query("SELECT p.id AS productId, p.title AS title, p.description AS description, " +
//            "p.runningTime AS runningTime, p.releaseDate AS releaseDate, p.category.name AS categoryName, " +
//            "SUM(pss.reservedQuantity) AS totalReservedCount " +
//            "FROM Product p " +
//                "JOIN ProductSeatSchedule pss ON pss.product.id = p.id " +
//            "GROUP BY p.id, p.title, p.description, p.runningTime, p.releaseDate, p.category.name  " +
//            "ORDER BY totalReservedCount DESC, p.releaseDate DESC, p.title")
//    List<PopularProductDto> findPagedPopularProduct(PageRequest pageRequest);

    @Query(value =     "SELECT " +
                        "    p.product_id AS productId, " +
                        "    p.title AS title, " +
                        "    p.description AS description, " +
                        "    p.running_time AS runningTime, " +
                        "    p.release_date AS releaseDate, " +
                        "    c.name AS categoryName, " +
                        "    t.totalReservedCount " +
                        "FROM product p " +
                        "JOIN category c ON p.category_id = c.category_id, " +
                        "     (SELECT pss.product_id, SUM(pss.reserved_quantity) AS totalReservedCount " +
                        "      FROM product_seat_schedule pss " +
                        "      GROUP BY pss.product_id) t " +
                        "WHERE p.product_id = t.product_id " +
                        "  AND (t.totalReservedCount < :lastCount " +
                        "       OR (t.totalReservedCount = :lastCount AND p.product_id < :lastProductId)) " +
                        "ORDER BY t.totalReservedCount DESC, p.product_id DESC " +
                        "LIMIT :limit",
            nativeQuery = true
    )
    List<PopularProductDto> findPagedPopularProduct(int limit, int lastProductId);

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

//    @Query("SELECT p.id AS productId, p.title AS title, p.description AS description, " +
//            "p.runningTime as runningTime, p.releaseDate as releaseDate, p.category.name AS categoryName, " +
//            " SUM(pss.reservedQuantity) as totalReservedCount " +
//            "FROM ProductSeatSchedule pss left join pss.product p " +
//            "WHERE p.category.id=:categoryId AND pss.reservedQuantity is not null " +
//            "GROUP BY p.id " +
//            "ORDER BY totalReservedCount DESC, p.releaseDate DESC")
//    List<PopularProductDto> findPopularProductByCategory(PageRequest pageRequest, Long categoryId);

    @Query(value = "SELECT " +
                    "    p.product_id AS productId, " +
                    "    p.title AS title, " +
                    "    p.description AS description, " +
                    "    p.running_time AS runningTime, " +
                    "    p.release_date AS releaseDate, " +
                    "    c.name AS categoryName, " +
                    "    t.totalReservedCount " +
                    "FROM product p " +
                    "JOIN category c ON p.category_id = c.category_id, " +
                    "     (SELECT pss.product_id, SUM(pss.reserved_quantity) AS totalReservedCount " +
                    "      FROM product_seat_schedule pss " +
                    "      GROUP BY pss.product_id) t " +
                    "WHERE p.category_id = :categoryId AND p.product_id = t.product_id " +
                    "  AND (t.totalReservedCount < :lastCount " +
                    "       OR (t.totalReservedCount = :lastCount AND p.product_id < :lastProductId)) " +
                    "ORDER BY t.totalReservedCount DESC, p.product_id DESC " +
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<PopularProductDto> findPopularProductByCategory(Long categoryId, int limit, int lastProductId);

    @Query(value = "SELECT pss.product_id AS productId, SUM(rp.reserved_price * rp.reserved_quantity) AS totalRevenue " +
            "FROM product_seat_schedule pss " +
            "JOIN reservation_price rp ON pss.product_seat_schedule_id = rp.product_seat_schedule_id " +
            "GROUP BY pss.product_id " +
            "ORDER BY totalRevenue DESC ", nativeQuery = true)
    List<ProductProfitDto> findPagedHighProfitProduct(PageRequest pageRequest);

    @Query(value = "SELECT p.product_id AS productId, " +
            "    SUM(rp.reserved_price * rp.reserved_quantity) AS totalRevenue " +
            "FROM product p " +
            "JOIN product_seat_schedule pss ON p.product_id = pss.product_id " +
            "JOIN reservation_price rp ON pss.product_seat_schedule_id = rp.product_seat_schedule_id " +
            "WHERE p.category_id = :categoryId " +
            "GROUP BY p.product_id " +
            "ORDER BY totalRevenue DESC ", nativeQuery = true)
    List<ProductProfitDto> findHighProfitProductByCategory(PageRequest pageRequest, Long categoryId);


}
