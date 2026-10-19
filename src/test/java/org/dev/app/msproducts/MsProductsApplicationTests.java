package org.dev.app.msproducts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class MsProductsApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--spring.main.web-application-type=none"};
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(MsProductsApplication.class, args)).thenReturn(null);

            MsProductsApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(MsProductsApplication.class, args));
        }
    }
}
