package kr.or.connect.reservation.domain.product.dto;

import kr.or.connect.reservation.domain.product.InMemoryProductDto;
import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class PopularProductResponse {
    private Long productId;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private Integer runningTime;
    private Integer totalReservationQuantity;
    private String category;

    @Builder
    public PopularProductResponse(Long productId, String title, String description, LocalDate releaseDate, Integer runningTime, Integer totalReservationQuantity, String categoryName) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.runningTime = runningTime;
        this.totalReservationQuantity = totalReservationQuantity;
        this.category = categoryName;
    }

    public static PopularProductResponse of(InMemoryProductDto inMemoryProductDto) {
        return PopularProductResponse.builder()
                .productId(inMemoryProductDto.getProductId())
                .title(inMemoryProductDto.getTitle())
                .description(inMemoryProductDto.getDescription())
                .releaseDate(inMemoryProductDto.getReleaseDate())
                .runningTime(inMemoryProductDto.getRunningTime())
                .totalReservationQuantity(inMemoryProductDto.getTotalReservedCount())
                .categoryName(inMemoryProductDto.getCategoryName())
                .build();
    }

    public static PopularProductResponse of(PopularProductDto popularProductDto) {
        return PopularProductResponse.builder()
                .productId(popularProductDto.getProductId())
                .title(popularProductDto.getTitle())
                .description(popularProductDto.getDescription())
                .releaseDate(popularProductDto.getReleaseDate())
                .runningTime(popularProductDto.getRunningTime())
                .totalReservationQuantity(popularProductDto.getTotalReservedCount())
                .categoryName(popularProductDto.getCategoryName())
                .build();
    }
}
