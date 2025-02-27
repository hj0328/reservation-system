package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Getter @Setter @ToString
public class InMemoryProductDto implements Serializable, Comparable<InMemoryProductDto> {
    private Long productId;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private Integer runningTime;
    private Integer totalReservedCount;

    @Builder
    public InMemoryProductDto(Long productId, String title, String description, LocalDate releaseDate, Integer runningTime, Integer totalReservedCount) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.runningTime = runningTime;
        this.totalReservedCount = totalReservedCount;
    }

    public static InMemoryProductDto of(PopularProductDto productDto) {
        return InMemoryProductDto.builder()
                .productId(productDto.getProductId())
                .description(productDto.getDescription())
                .title(productDto.getTitle())
                .releaseDate(productDto.getReleaseDate())
                .runningTime(productDto.getRunningTime())
                .totalReservedCount(productDto.getTotalReservedCount())
                .build();
    }

    public void addReservedQuantity(Integer reservedQuantity) {
        this.totalReservedCount += reservedQuantity;
    }

    public void minusReservedQuantity(Integer reservedQuantity) {
        this.totalReservedCount -= reservedQuantity;
    }

    @Override
    public int compareTo(InMemoryProductDto o) {
        // 먼저 productId로 비교 (null 체크도 필요)
        if (this.productId != null && o.productId != null) {
            int cmp = this.productId.compareTo(o.productId);
            if (cmp != 0) {
                return cmp;
            }
        }
        // 만약 productId가 null이거나 동일하다면, totalReservedCount로 비교
        if (this.totalReservedCount != null && o.totalReservedCount != null) {
            return this.totalReservedCount.compareTo(o.totalReservedCount);
        }
        return 0;
    }
}
