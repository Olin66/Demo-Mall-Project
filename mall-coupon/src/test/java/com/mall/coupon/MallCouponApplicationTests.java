package com.mall.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@SpringBootTest
class MallCouponApplicationTests {

    @Test
    void contextLoads() {
        LocalDate now = LocalDate.now();
        LocalDate plus1 = now.plusDays(1);
        LocalDate plus2 = now.plusDays(2);
        System.out.println(now);
        System.out.println(plus1);
        System.out.println(plus2);
        LocalTime max = LocalTime.MAX;
        LocalTime min = LocalTime.MIN;
        System.out.println(max);
        System.out.println(min);
        LocalDateTime start = LocalDateTime.of(now, min);
        LocalDateTime end = LocalDateTime.of(plus2, max);
        System.out.println(start);
        System.out.println(end);
    }

}
