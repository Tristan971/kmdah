package moe.tristan.kmdah.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KmdahWorker {

    public static void main(String[] args) {
        SpringApplication.run(KmdahWorker.class);
    }

}
