package kr.or.connect.reservation.domain.product.dao;

import kr.or.connect.reservation.domain.product.entity.Product;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id > :productId ORDER BY p.id, p.releaseDate Desc, p.title")
    List<Product> findAllByCategoryId(Long productId, Long categoryId, PageRequest pageRequest);

    Long countByCategoryId(Long categoryId);

    @Query(value = "SELECT product0_.product_id, product0_.created_at, product0_.updated_at , product0_.category_id, product0_.description, product0_.release_date , product0_.running_time , product0_.title  " +
            "FROM product product0_  " +
            "WHERE product0_.product_id > :productId AND product0_.title LIKE CONCAT(:title, '%') " +
            "ORDER BY product0_.product_id, product0_.title " +
            "LIMIT :pageSize ", nativeQuery = true)
    List<Product> findByTitleStartsWith(String title, Long productId, Integer pageSize);

    @Query(value = "SELECT product0_.product_id, product0_.created_at, product0_.updated_at , product0_.category_id, product0_.description, product0_.release_date , product0_.running_time , product0_.title  " +
            "FROM product product0_  " +
            "WHERE product0_.title LIKE CONCAT(:title, '%') " +
            "ORDER BY product0_.product_id, product0_.title " +
            "LIMIT :pageSize OFFSET :offset ", nativeQuery = true)
    List<Product> findByTitleStartsWithTemp(String title, Integer offset, Integer pageSize);

    @Query(value = "SELECT product0_.product_id , product0_.created_at, product0_.updated_at, product0_.category_id, product0_.description, product0_.release_date , product0_.running_time , product0_.title  " +
            "FROM product product0_ left outer join category category1_ " +
                "on product0_.category_id=category1_.category_id  " +
            "WHERE product0_.product_id > :productId AND product0_.title LIKE CONCAT(:title, '%') AND category1_.category_id=:categoryId " +
            "ORDER BY product0_.product_id, product0_.title " +
            "LIMIT :pageSize ", nativeQuery = true)
    List<Product> findByTitleStartingWithAndCategoryId(String title, Long categoryId, Long productId, Integer pageSize);

    @Query(value = "SELECT product0_.product_id , product0_.created_at, product0_.updated_at, product0_.category_id, product0_.description, product0_.release_date , product0_.running_time , product0_.title  " +
            "FROM product product0_ left outer join category category1_ " +
            "on product0_.category_id=category1_.category_id  " +
            "WHERE product0_.title LIKE CONCAT(:title, '%') AND category1_.category_id=:categoryId " +
            "ORDER BY product0_.product_id, product0_.title " +
            "LIMIT :pageSize OFFSET :offset ", nativeQuery = true)
    List<Product> findByTitleStartingWithAndCategoryIdTemp(String title, Long categoryId, Integer offset, Integer pageSize);


    @Query("SELECT p FROM Product p WHERE p.id > :productId ORDER BY p.id, p.releaseDate Desc, p.title")
    List<Product> findAllProducts(Long productId, PageRequest pageRequest);

    @Query("SELECT p FROM Product p ORDER BY p.id, p.releaseDate Desc, p.title")
    List<Product> findAllProductsTemp(Long productId, PageRequest pageRequest);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId ORDER BY p.id, p.releaseDate Desc, p.title")
    List<Product> findAllByCategoryIdTemp(Long categoryId, PageRequest pageRequest);

    @Query(value = "SELECT product0_.product_id, product0_.created_at, product0_.updated_at , product0_.category_id, product0_.description, product0_.release_date , product0_.running_time , product0_.title  " +
            "FROM product product0_  " +
            "WHERE product0_.release_date >= :startDate " +
            "ORDER BY product0_.product_id, product0_.release_date " +
            "LIMIT :pageSize OFFSET :i ", nativeQuery = true)
    List<Product> findProductsAfterDateTemp(LocalDate startDate, long i, Integer pageSize);

    @Query(value = "SELECT product0_.product_id, product0_.created_at, product0_.updated_at , product0_.category_id, product0_.description, product0_.release_date , product0_.running_time , product0_.title  " +
            "FROM product product0_  " +
            "WHERE product0_.release_date >= :startDate AND product0_.category_id = :categoryId " +
            "ORDER BY product0_.product_id, product0_.release_date " +
            "LIMIT :pageSize OFFSET :i ", nativeQuery = true)
    List<Product> findProductsAfterDateByCategoryTemp(LocalDate startDate, Long categoryId, long i, Integer pageSize);
}
