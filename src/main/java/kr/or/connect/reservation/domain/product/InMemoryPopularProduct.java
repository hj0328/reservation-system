package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.product.dto.PopularProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryPopularProduct {

    private Map<Long, InMemoryProductDto> popularProductMap = new HashMap<>();
    private List<Long> sortedProductIds = new ArrayList<>();

    public synchronized void refresh(List<InMemoryProductDto> newPopularProducts) {
        log.info("refresh popular product");
        this.popularProductMap = newPopularProducts.stream()
                .collect(Collectors.toMap(InMemoryProductDto::getProductId, product -> product));

        this.sortedProductIds = newPopularProducts.stream()
                .map(InMemoryProductDto::getProductId)
                .collect(Collectors.toList());
    }

    public List<PopularProductResponse> getProducts(int offset, int limit) {
        log.debug("get popular product from cache. offset={}, limit={}", offset, limit);
        int fromIndex = offset;
        int toIndex = offset + limit;
        int idsSize = sortedProductIds.size();

        if (offset > idsSize) {
            return Collections.emptyList();
        }

        if (toIndex > idsSize) {
            toIndex = idsSize;
        }

        List<Long> subIds = sortedProductIds.subList(fromIndex, toIndex);
        return subIds.stream()
                .map(this.popularProductMap::get)
                .map(PopularProductResponse::of)
                .sorted(Comparator.comparing(PopularProductResponse::getTotalReservationQuantity).reversed())
                .collect(Collectors.toList());
    }

    // 예약 등록 -> productId로 찾아서 개수 감소 -> 순서가 바뀐다면 swap 처리
    public synchronized void reserve(InMemoryProductDto newInMemoryProductDto) {
        Long productId = newInMemoryProductDto.getProductId();

        if (this.popularProductMap.containsKey(productId)) {
            InMemoryProductDto inMemoryProduct = this.popularProductMap.get(productId);
            inMemoryProduct.addReservedQuantity(newInMemoryProductDto.getTotalReservedCount());

            updateOrder(inMemoryProduct, productId);
        } else {
            popularProductMap.put(productId, newInMemoryProductDto);
            sortedProductIds.add(productId);
        }
    }

    public synchronized void cancel(InMemoryProductDto newInMemoryProductDto) {
        Long productId = newInMemoryProductDto.getProductId();

        if (this.popularProductMap.containsKey(productId)) {
            InMemoryProductDto inMemoryProduct = this.popularProductMap.get(productId);
            inMemoryProduct.minusReservedQuantity(newInMemoryProductDto.getTotalReservedCount());

            updateOrder(inMemoryProduct, productId);
        }
    }

    private void updateOrder(InMemoryProductDto updateInMemoryProduct, Long productId) {
        int currentIdx = this.sortedProductIds.indexOf(productId);

        if (currentIdx == -1) {
            return;
        }

        moveForward(updateInMemoryProduct, currentIdx);
        moveBackward(updateInMemoryProduct, currentIdx);
    }

    // product의 예약 건수는 앞쪽일수록 크다.
    private void moveForward(InMemoryProductDto updateInMemoryProduct, int currentIdx) {
        while (canSwapWithNextProduct(updateInMemoryProduct, currentIdx)) {
            swap(currentIdx, currentIdx - 1);
            currentIdx--;
        }
    }

    // product의 예약 건수는 뒤쪽일수록 작다.
    private void moveBackward(InMemoryProductDto updateInMemoryProduct, int currentIdx) {
        while (canSwapWithPreviousProduct(updateInMemoryProduct, currentIdx)) {
            swap(currentIdx, currentIdx + 1);
            currentIdx++;
        }
    }

    private boolean canSwapWithPreviousProduct(InMemoryProductDto updateInMemoryProduct, int currentIdx) {
        int nextIds = currentIdx + 1;
        if (nextIds == this.sortedProductIds.size()) {
            return false;
        }

        InMemoryProductDto nextInMemoryProductDto = this.popularProductMap.get((long) nextIds);
        if (nextInMemoryProductDto == null || nextInMemoryProductDto.getTotalReservedCount() == null) {
            return true;
        }
        if (updateInMemoryProduct.getTotalReservedCount() < nextInMemoryProductDto.getTotalReservedCount()) {
            return true;
        }

        return false;
    }

    private boolean canSwapWithNextProduct(InMemoryProductDto updateInMemoryProduct, int currentIdx) {
        int previousIdx = currentIdx - 1;
        if (previousIdx < 0) {
            return false;
        }

        InMemoryProductDto preInMemoryProductDto = this.popularProductMap.get((long) previousIdx);
        if (preInMemoryProductDto == null || preInMemoryProductDto.getTotalReservedCount() == null) {
            return true;
        }
        if (updateInMemoryProduct.getTotalReservedCount() > preInMemoryProductDto.getTotalReservedCount()) {
            return true;
        }

        return false;
    }

    private void swap(int idx1, int idx2) {
        Long productId1 = this.sortedProductIds.get(idx1);
        this.sortedProductIds.set(idx1, this.sortedProductIds.get(idx2));
        this.sortedProductIds.set(idx2, productId1);
    }

    public boolean isEmpty() {
        return popularProductMap.isEmpty();
    }
}
