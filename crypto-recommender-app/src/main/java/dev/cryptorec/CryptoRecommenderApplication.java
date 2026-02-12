package dev.cryptorec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dev.cryptorec")
public class CryptoRecommenderApplication {
    public static void main(String[] args) {
        SpringApplication.run(CryptoRecommenderApplication.class, args);
    }
}
