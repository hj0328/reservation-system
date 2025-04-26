package kr.or.connect.reservation.domain.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScript;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPopularProduct {
    private final ProductSeatScheduleRepository productSeatScheduleRepository;
    private final RedissonClient redissonClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());


    @EventListener(ApplicationReadyEvent.class)
    public void warmUpRedisWithLuaScript() {
        new Thread(this::initializeWithLuaScript).start();
    }

    public void initializeWithLuaScript() {
        try {
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            List<PopularProductDto> popularProductDtos = productSeatScheduleRepository.findAllPopularProducts();
            List<InMemoryProductDto> productDtos = popularProductDtos.stream()
                    .map(InMemoryProductDto::of)
                    .collect(Collectors.toList());

            Set<InMemoryProductDto> uniqueDtos = new HashSet<>(productDtos); // 중복 제거

            String redisKey = "popularProducts";

            // Lua Script: 기존 데이터 삭제 + 새로 삽입
            String luaScript =
                    "redis.call('DEL', KEYS[1]); " +
                    "for i=1, #ARGV, 2 do " +
                    "  redis.call('ZADD', KEYS[1], ARGV[i], ARGV[i+1]); " +
                    "end";

            List<Object> args = new ArrayList<>();
            for (InMemoryProductDto dto : uniqueDtos) {
                args.add(dto.getTotalReservedCount());  // score
                args.add(objectMapper.writeValueAsString(dto)); // value: JSON 직렬화해서 저장
            }


            RScript script = redissonClient.getScript();
            script.eval(
                    RScript.Mode.READ_WRITE,
                    luaScript,
                    RScript.ReturnType.VALUE,
                    Collections.singletonList(redisKey),
                    args.toArray()
            );

            log.info("Redis Lua Script Popular Product Size={}", uniqueDtos.size());

        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패", e);
            throw new RuntimeException("Redis 초기화 중 직렬화 실패", e);
        }
    }

    /**
     *  기동 시 Redis에 cache warmup
     */
    //@PostConstruct
    public void initialize() {
        List<PopularProductDto> popularProductDtos = productSeatScheduleRepository
                .findAllPopularProducts();

        List<InMemoryProductDto> productDtos = popularProductDtos.stream()
                .map(InMemoryProductDto::of)
                .collect(Collectors.toList());

        RSortedSet<InMemoryProductDto> redissonClientSortedSet = redissonClient.getSortedSet("popularProducts");
        redissonClientSortedSet.clear(); // 기존 데이터 삭제 후

        // 기존 데이터를 가져와 Set으로 중복 제거
        Set<InMemoryProductDto> existingSet = new HashSet<>();
        existingSet.addAll(productDtos); // 중복 제거 후 추가

        redissonClientSortedSet.addAll(existingSet); // 중복 제거된 데이터 삽입

        log.info("Redis Popular Product Size={}", redissonClientSortedSet.size());
    }

    public List<InMemoryProductDto> getProductDtos() {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet("popularProducts");

        List<InMemoryProductDto> result = new ArrayList<>();
        for (String json : sortedSet) {
            try {
                InMemoryProductDto dto = objectMapper.readValue(json, InMemoryProductDto.class);
                result.add(dto);
            } catch (Exception e) {
                log.error("Exception Message=",e.getMessage());
                log.error("Exception=",e.getStackTrace()[0]);
            }
        }

        return result;
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
