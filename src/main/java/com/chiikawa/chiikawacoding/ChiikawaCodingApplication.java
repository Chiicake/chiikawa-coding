package com.chiikawa.chiikawacoding;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.chiikawa.chiikawacoding.mapper")
public class ChiikawaCodingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChiikawaCodingApplication.class, args);
    }

}
