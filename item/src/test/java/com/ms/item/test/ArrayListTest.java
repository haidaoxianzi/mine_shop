package com.ms.item.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: gina
 * @Date: 2025-03-08
 * @Description:数组元素较大，定义数组时建议初始化数组大小，这样可以提高性能
 */
@Slf4j
@SpringBootTest
public class ArrayListTest {
    @Test
    void testArrayList() {
        List<String> strList = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            strList.add("Nancy_" + i);
        }
        long start1 = System.currentTimeMillis();
        log.info("耗时：{}", start1 - start);
        List<String> strList2 = new ArrayList<>(1000000);
        for (int i = 0; i < 1000000; i++) {
            strList2.add("Hallo_" + i);
        }
        long end = System.currentTimeMillis();
        log.info("耗时：{}", end - start1);
    }
}
