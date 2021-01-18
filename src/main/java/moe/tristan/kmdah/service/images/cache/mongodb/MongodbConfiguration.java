package moe.tristan.kmdah.service.images.cache.mongodb;

import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
@Profile("mongodb")
@EnableConfigurationProperties(MongodbSettings.class)
@Import(value = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
public class MongodbConfiguration {

    @Bean
    MongodbCachedImageService mongodbCachedImageService(MongoTemplate mongoTemplate, GridFsTemplate gridFsTemplate) {
        return new MongodbCachedImageService(mongoTemplate, gridFsTemplate);
    }

}
