package moe.tristan.kmdah.operator;

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
