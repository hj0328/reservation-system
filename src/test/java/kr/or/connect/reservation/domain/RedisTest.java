package kr.or.connect.reservation.domain;

import kr.or.connect.reservation.domain.product.InMemoryProductDto;
import kr.or.connect.reservation.domain.product.RedisPopularProduct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RBucket;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisPopularProduct redisInitializer;

    @Autowired
    private ApplicationContext context;

    @Test
    public void redisSaveTest () throws Exception {
        // given
        RBucket<InMemoryProductDto> bucket = redissonClient.getBucket("redis-test");

        // when
        InMemoryProductDto inMemoryProductDto = InMemoryProductDto.builder()
                .title("testTitle")
                .build();

        bucket.set(inMemoryProductDto);

        // then
        InMemoryProductDto inMemoryProductDto1 = bucket.get();
        System.out.println(inMemoryProductDto1.getTitle());
        assertThat("testTitle").isEqualTo(inMemoryProductDto1.getTitle());
    }

    @Test
    public void listSave () throws Exception {
        // given
        RSortedSet<InMemoryProductDto> testSortTedSet = redissonClient.getSortedSet("testSortTedSet");
        testSortTedSet.clear();

        InMemoryProductDto inMemoryProductDto1 = InMemoryProductDto.builder()
                .title("testTitle")
                .totalReservedCount(1)
                .build();

        InMemoryProductDto inMemoryProductDto2 = InMemoryProductDto.builder()
                .title("testTitle")
                .totalReservedCount(3)
                .build();

        InMemoryProductDto inMemoryProductDto3 = InMemoryProductDto.builder()
                .title("testTitle")
                .totalReservedCount(2)
                .build();

        // when
        testSortTedSet.add(inMemoryProductDto1);
        testSortTedSet.add(inMemoryProductDto2);
        testSortTedSet.add(inMemoryProductDto3);

        // then
        RSortedSet<InMemoryProductDto> retBucket = redissonClient.getSortedSet("testSortTedSet");

        List<Integer> collect = retBucket.stream()
                .map(v -> v.getTotalReservedCount())
                .collect(Collectors.toList());
        assertThat(collect).containsExactly(1, 2, 3);
    }

    @Test
    public void updateRedis () throws Exception {
        // given
        RSortedSet<InMemoryProductDto> testSortTedSet = redissonClient.getSortedSet("testSortTedSetUpdate");
        testSortTedSet.clear();

        InMemoryProductDto inMemoryProductDto1 = InMemoryProductDto.builder()
                .title("testTitle1")
                .totalReservedCount(1)
                .build();
        testSortTedSet.add(inMemoryProductDto1);

        InMemoryProductDto inMemoryProductDto2 = InMemoryProductDto.builder()
                .title("testTitle2")
                .totalReservedCount(2)
                .build();
        testSortTedSet.add(inMemoryProductDto2);

        // when
        InMemoryProductDto target = null;
        RSortedSet<InMemoryProductDto> retBucket = redissonClient.getSortedSet("testSortTedSetUpdate");
        for (InMemoryProductDto inMemoryProductDto : retBucket) {
            if (inMemoryProductDto.getTitle().equals("testTitle2")) {
                target = inMemoryProductDto;
            }
        }
        retBucket.remove(target);

        InMemoryProductDto inMemoryProductDto3 = InMemoryProductDto.builder()
                .title("changeTitle")
                .totalReservedCount(3)
                .build();
        testSortTedSet.add(inMemoryProductDto3);

        // then
        RSortedSet<InMemoryProductDto> result = redissonClient.getSortedSet("testSortTedSetUpdate");
        assertThat(result.contains(inMemoryProductDto3));
    }
}
