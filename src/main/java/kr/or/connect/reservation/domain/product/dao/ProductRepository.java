package kr.or.connect.reservation.domain.product.dao;

import kr.or.connect.reservation.domain.product.entity.Product;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id > :productId ORDER BY p.id")
    List<Product> findAllByCategoryId(Long productId, Long categoryId, PageRequest pageRequest);

    Long countByCategoryId(Long categoryId);

    @Query(value = "SELECT product0_.product_id as product_1_3_, product0_.created_at as created_2_3_, product0_.updated_at as updated_3_3_, product0_.category_id as category8_3_, product0_.description as descript4_3_, product0_.release_date as release_5_3_, product0_.running_time as running_6_3_, product0_.title as title7_3_ " +
            "FROM product product0_  " +
            "WHERE product0_.product_id > :productId AND product0_.title LIKE CONCAT(:title, '%') " +
            "ORDER BY product0_.product_id, product0_.title " +
            "LIMIT :pageSize ", nativeQuery = true)
    List<Product> findByTitleStartsWith(String title, Long productId, Integer pageSize);

    @Query(value = "SELECT product0_.product_id as product_1_3_, product0_.created_at as created_2_3_, product0_.updated_at as updated_3_3_, product0_.category_id as category8_3_, product0_.description as descript4_3_, product0_.release_date as release_5_3_, product0_.running_time as running_6_3_, product0_.title as title7_3_ " +
            "FROM product product0_ left outer join category category1_ " +
                "on product0_.category_id=category1_.category_id  " +
            "WHERE product0_.product_id > :productId AND product0_.title LIKE CONCAT(:title, '%') AND category1_.category_id=:categoryId " +
            "ORDER BY product0_.product_id, product0_.title " +
            "LIMIT :pageSize ", nativeQuery = true)
    List<Product> findByTitleStartingWithAndCategoryId(String title, Long categoryId, Long productId, Integer pageSize);

    @Query("SELECT p FROM Product p WHERE p.id > :productId ORDER BY p.id")
    List<Product> findAllProducts(Long productId, PageRequest pageRequest);
}
