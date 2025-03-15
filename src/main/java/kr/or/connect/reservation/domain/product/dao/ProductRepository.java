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

    List<Product> findByTitleStartsWith(String title);

    List<Product> findByTitleStartingWithAndCategoryId(String title, Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.id > :productId ORDER BY p.id")
    List<Product> findAllProducts(Long productId, PageRequest pageRequest);
}
