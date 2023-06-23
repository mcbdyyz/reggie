package com.itheima.reggie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReggieTaskOutApplicationTests {

    @Test
    void contextLoads() {
        String a = new String("java1");
        a.intern();
        System.out.println(a ==  "java1");

    }

}
