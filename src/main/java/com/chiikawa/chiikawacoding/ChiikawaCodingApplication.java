package com.chiikawa.chiikawacoding;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.chiikawa.chiikawacoding.mapper")
public class ChiikawaCodingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChiikawaCodingApplication.class, args);
    }

}
