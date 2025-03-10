package kr.or.connect.reservation.domain.category;

import kr.or.connect.reservation.domain.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
//    @Query("SELECT count(*) AS count, c.id AS id, c.name AS name " +
//            "FROM Product p LEFT JOIN p.category" +
//            "GROUP BY c.name, c.id")
//    List<CategoryGroupByProductDto> findCategoryAndProductCountGroupByName();
}
