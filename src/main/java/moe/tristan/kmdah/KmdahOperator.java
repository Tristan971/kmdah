package moe.tristan.kmdah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KmdahOperator {

    public static void main(String[] args) {
        SpringApplication.run(KmdahOperator.class);
    }

}
