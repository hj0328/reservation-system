package kr.or.connect.reservation.domain.reservation;

import kr.or.connect.reservation.domain.config.RedisConfig;
import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import kr.or.connect.reservation.domain.product.entity.ProductSeatSchedule;
import kr.or.connect.reservation.domain.product.entity.SeatType;
import kr.or.connect.reservation.domain.reservation.dto.NewReservationRequest;
import kr.or.connect.reservation.domain.reservation.dto.ReservationPriceDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(classes = {RedisConfig.class})
//@Transactional
@Sql(scripts = {"classpath:data/data.sql", "classpath:data/member-data.sql"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)    // h2에 추가한 sql 초기화
class ReservationServiceBootTest {

    @Autowired
    private ProductSeatScheduleRepository productSeatScheduleRepository;
    @Autowired
    private ReservationService reservationService;

    @Test
    public void 예약_100개_동시_처리() throws Exception {
        // given
        int threadCount = 10000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        Queue<NewReservationRequest> queue = new ConcurrentLinkedQueue<>();
        for (int i = 1; i <= threadCount; i++) {
            ReservationPriceDto price = ReservationPriceDto.builder()
                    .productSeatScheduleId(1L)
                    .price(10)
                    .quantity(1)
                    .seatType(SeatType.S.name())
                    .placeId(1)
                    .build();
            List<ReservationPriceDto> list1 = new ArrayList<>();
            list1.add(price);

            NewReservationRequest reservationRequest = NewReservationRequest.builder()
                    .memberId((long) i)
                    .productId(1L)
                    .reservationPriceDtos(list1)
                    .build();

            queue.add(reservationRequest);
        }

        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 1; i <= threadCount; i++) {
            executorService.submit(() -> {
                try {
                    NewReservationRequest req = queue.poll();
                    if (req != null) {
                        reservationService.createReservation(req);
                    }
                } catch (Exception e) {
                    System.out.println(e.getStackTrace()[0]);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        ProductSeatSchedule ret = productSeatScheduleRepository.findProductSeatById(1L).get();
        Assertions.assertThat(ret.getReservedQuantity()).isEqualTo(threadCount);
    }

    @Test
    public void 따닥_요청_방지_테스트() throws Exception {
        // given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        Queue<NewReservationRequest> queue = new ConcurrentLinkedQueue<>();
        for (int i = 1; i <= threadCount; i++) {
            ReservationPriceDto price = ReservationPriceDto.builder()
                    .productSeatScheduleId(1L)
                    .price(10)
                    .quantity(1)
                    .seatType(SeatType.S.name())
                    .placeId(31)
                    .build();
            List<ReservationPriceDto> list1 = new ArrayList<>();
            list1.add(price);

            NewReservationRequest reservationRequest = NewReservationRequest.builder()
                    .memberId(1001L)
                    .productId(1L)
                    .reservationPriceDtos(list1)
                    .build();

            queue.add(reservationRequest);
        }

        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 1; i <= threadCount; i++) {
            executorService.submit(() -> {
                try {
                    NewReservationRequest req = queue.poll();
                    if (req != null) {
                        try {
                            reservationService.createReservation(req);
                        } catch (Exception e) {
                            // 예외는 당연히 발생해야하기 때문에 예외처리
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        ProductSeatSchedule ret = productSeatScheduleRepository.findProductSeatById(1L).get();

        // 1건만 성공
        Assertions.assertThat(ret.getReservedQuantity()).isEqualTo(1);
    }
}