package moe.tristan.kmdah.service.images.cache.mongodb;

import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;

@Configuration
@Profile("cache-mongodb")
@EnableConfigurationProperties(MongodbSettings.class)
@Import(value = {
    MongoReactiveAutoConfiguration.class,
    MongoReactiveDataAutoConfiguration.class
})
public class MongodbConfiguration {

    @Bean
    MongodbCachedImageService mongodbCachedImageService(ReactiveGridFsTemplate reactiveGridFsTemplate, ReactiveMongoTemplate reactiveMongoTemplate) {
        return new MongodbCachedImageService(reactiveGridFsTemplate, reactiveMongoTemplate);
    }

}
