package kr.or.connect.reservation.domain.product.dao;

import kr.or.connect.reservation.domain.product.InMemoryProductDto;
import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import kr.or.connect.reservation.domain.product.entity.ProductSeatSchedule;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ProductSeatScheduleRepository extends JpaRepository<ProductSeatSchedule, Long> {
    List<ProductSeatSchedule> findAllByProductId(Long productId);

    /*
     * 예매 시 동시성 이슈 방지
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductSeatSchedule> findById(Long id);

    @Query("SELECT p.id as productId, p.title as title, p.description as description, " +
            "p.runningTime as runningTime, p.releaseDate as releaseDate, p.category.name AS categoryName, " +
            "SUM(pss.reservedQuantity) as totalReservedCount " +
            "FROM Product p " +
                "JOIN ProductSeatSchedule pss ON pss.product.id = p.id " +
            "GROUP BY p.id " +
            "ORDER BY totalReservedCount DESC")
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
            "ORDER BY totalReservedCount DESC")
    List<PopularProductDto> findAllPopularProductRedis();


    @Query(value = "select p.product_id as productId, p.title as title, p.description as description, " +
            "p.running_Time as runningTime, p.release_date as releaseDate, " +
            "sub.totalReservedCount \n" +
            "from product p, category c \n" +
                " (select pss.product_id, sum(pss.reserved_quantity) as totalReservedCount  \n" +
                " from product_seat_schedule pss \n" +
                " group by pss.product_id) as sub \n" +
            "where p.product_id = sub.product_id, p.category_id = c.category_id \n" +
            "order by totalReservedCount desc \n", nativeQuery = true)
    List<InMemoryProductDto> findAllPopularProductInMemoryProduct();

    @Query("SELECT p.id as productId, p.title as title, p.description as description, \n" +
            "p.runningTime as runningTime, p.releaseDate as releaseDate, p.category.name AS categoryName, \n" +
            " SUM(pss.reservedQuantity) as totalReservedCount \n" +
            "FROM ProductSeatSchedule pss left join pss.product p \n" +
            "WHERE p.category.id=:categoryId AND pss.reservedQuantity is not null \n" +
            "group by p.id \n" +
            "order by totalReservedCount desc \n")
    List<PopularProductDto> findPopularProductByCategory(PageRequest pageRequest, @Param("categoryId") Long categoryId);


}
