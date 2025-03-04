package kr.or.connect.reservation.domain.product.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Getter
public class ProductRegisterRequest {
    @NotNull
    private Long categoryId;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    private Integer runningTime;
    @NotNull
    private List<ProductPriceRequest> priceList;

    @Builder
    public ProductRegisterRequest(Long categoryId, String title, String description,
                                  LocalDate releaseDate, Integer runningTime,
                                  List<ProductPriceRequest> priceList) {
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.runningTime = runningTime;
        this.priceList = priceList;
    }
}
