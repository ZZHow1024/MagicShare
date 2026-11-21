package com.zzhow.magicshare;

import com.zzhow.magicshare.util.Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class MagicShareApplicationTests {

    @Test
    void contextLoads() {
    }

}
