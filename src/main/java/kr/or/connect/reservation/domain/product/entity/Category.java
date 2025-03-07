package kr.or.connect.reservation.domain.product.entity;

import kr.or.connect.reservation.config.exception.CustomException;
import kr.or.connect.reservation.config.exception.CustomExceptionStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Arrays;

@Entity
@Getter
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, unique = true)
    private CategoryType name;

    private Category(CategoryType categoryType) {
        this.name = categoryType;
    }

    public static Category createCategory(CategoryType categoryType) {
        return new Category(categoryType);
    }

    public void updateCategory(String category) {
        validate(category);
        this.name = CategoryType.valueOf(category);
    }

    private void validate(String category) {
        if (Arrays.stream(CategoryType.values())
                .noneMatch(v -> v.toString().equals(category))) {
            throw new CustomException(CustomExceptionStatus.CATEGORY_NOT_FOUND);
        }

    }
}
