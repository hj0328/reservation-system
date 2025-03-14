package kr.or.connect.reservation.domain.product.dao;

import kr.or.connect.reservation.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByCategoryId(Long categoryId, PageRequest pageRequest);

    Long countByCategoryId(Long categoryId);

    List<Product> findByTitleStartsWith(String title);

    List<Product> findByTitleStartingWithAndCategoryId(String title, Long categoryId);
}
