package moe.tristan.kmdah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;

@SpringBootApplication(
    exclude = MongoReactiveAutoConfiguration.class // do not automatically import mongo config, in case filesystem cache is used instead
)
public class Kmdah {

    public static void main(String[] args) {
        SpringApplication.run(Kmdah.class);
    }

}
