package kr.or.connect.reservation.domain.reservation;

import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import kr.or.connect.reservation.domain.product.entity.ProductSeatSchedule;
import kr.or.connect.reservation.domain.product.entity.SeatType;
import kr.or.connect.reservation.domain.reservation.dto.NewReservationRequest;
import kr.or.connect.reservation.domain.reservation.dto.ReservationPriceDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"classpath:data/data.sql"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)    // h2에 추가한 sql 초기화
class ReservationServiceBootTest {

    @Autowired
    private ProductSeatScheduleRepository productSeatScheduleRepository;
    @Autowired
    private ReservationService reservationService;

    @Test
    public void 예약_100개_동시_처리() throws Exception {
        // given
        int threadCount = 100;
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
                    .memberId(1L)
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
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        ProductSeatSchedule ret = productSeatScheduleRepository.findById(1L).get();
        Assertions.assertThat(ret.getReservedQuantity()).isEqualTo(100);
    }
}