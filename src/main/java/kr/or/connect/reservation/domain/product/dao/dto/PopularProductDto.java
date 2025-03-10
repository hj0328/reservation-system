package kr.or.connect.reservation.domain.product.dao.dto;

import java.time.LocalDate;

public interface PopularProductDto {
    Long getProductId();
    String getTitle();
    String getDescription();
    LocalDate getReleaseDate();
    Integer getRunningTime();
    Integer getTotalReservedCount();
    String getCategoryName();
}
