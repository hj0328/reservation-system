package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InMemoryProductScheduler {

    private final ProductSeatScheduleRepository productSeatScheduleRepository;
    private final InMemoryPopularProduct inMemoryPopularProduct;

//    @PostConstruct
//    public void initialize() {
//        refreshPopularProduct();
//    }
//
//    @Scheduled(fixedDelay = 600000)
//    public void refreshPopularProduct() {
//        List<PopularProductDto> popularProductDtos = productSeatScheduleRepository
//                .findAllPopularProducts();
//
//        List<InMemoryProductDto> inMemoryProductDtos = popularProductDtos.stream()
//                .map(InMemoryProductDto::of)
//                .collect(Collectors.toList());
//        inMemoryPopularProduct.refresh(inMemoryProductDtos);
//    }
}
