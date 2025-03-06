package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPopularProduct {
    private final ProductSeatScheduleRepository productSeatScheduleRepository;
    private final RedissonClient redissonClient;

    /**
     *  기동 시 Redis에 cache warmup
     */
    @PostConstruct
    public void initialize() {
        List<PopularProductDto> popularProductDtos = productSeatScheduleRepository
                .findAllPopularProductByReservation();

        List<InMemoryProductDto> productDtos = popularProductDtos.stream()
                .map(InMemoryProductDto::of)
                .collect(Collectors.toList());

        RSortedSet<InMemoryProductDto> redissonClientSortedSet = redissonClient.getSortedSet("popularProducts");
        redissonClientSortedSet.clear(); // 기존 데이터 삭제 후

        // 기존 데이터를 가져와 Set으로 중복 제거
        Set<InMemoryProductDto> existingSet = new HashSet<>(redissonClientSortedSet.readAll());
        existingSet.addAll(productDtos); // 중복 제거 후 추가

        redissonClientSortedSet.addAll(existingSet); // 중복 제거된 데이터 삽입

        log.info("Redis Popular Product Size={}", redissonClientSortedSet.size());
    }

    public List<InMemoryProductDto> getProductDtos() {
        RSortedSet<InMemoryProductDto> redissonClientSortedSet = redissonClient.getSortedSet("popularProducts");
        return new ArrayList<>(redissonClientSortedSet);
    }

    /**
     * Redis에 새로운 Product 등록
     */
    public void register(InMemoryProductDto saveProductDto) {
        InMemoryProductDto target = null;
        RSortedSet<InMemoryProductDto> redissonClientSortedSet = redissonClient.getSortedSet("popularProducts");
        for (InMemoryProductDto redisProductDto : redissonClientSortedSet) {
            if (!Objects.equals(redisProductDto.getProductId(), saveProductDto.getProductId())) {
                continue;
            }

            target = redisProductDto;
            break;
        }

        if (target == null) {
            redissonClientSortedSet.add(saveProductDto);
        }
    }

    /**
     * Redis 등록 Product의 예약 취소
     * @param productId
     * @param cancelCount
     */
    public void cancel(Long productId, Integer cancelCount) {
        InMemoryProductDto target = null;
        RSortedSet<InMemoryProductDto> redissonClientSortedSet = redissonClient.getSortedSet("popularProducts");
        for (InMemoryProductDto redisProductDto : redissonClientSortedSet) {
            if(productId.equals(redisProductDto.getProductId())){
                target = redisProductDto;
                break;
            }
        }

        if (target != null) {
            redissonClientSortedSet.remove(target);
            target.minusReservedQuantity(cancelCount);
            redissonClientSortedSet.add(target);
        }
    }

    /**
     * Redis 등록 Product에 대한 예약
     * @param productId
     * @param reserveCount
     */
    public void reserve(Long productId, Integer reserveCount) {
        InMemoryProductDto target = null;
        RSortedSet<InMemoryProductDto> redissonClientSortedSet = redissonClient.getSortedSet("popularProducts");
        for (InMemoryProductDto redisProductDto : redissonClientSortedSet) {
            if (productId.equals(redisProductDto.getProductId())) {
                target = redisProductDto;
                break;
            }
        }

        if (target != null) {
            redissonClientSortedSet.remove(target);
            target.addReservedQuantity(reserveCount);
            redissonClientSortedSet.add(target);
        }
    }
}
