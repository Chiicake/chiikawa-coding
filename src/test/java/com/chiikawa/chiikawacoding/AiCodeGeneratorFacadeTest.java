package com.chiikawa.chiikawacoding;

import com.chiikawa.chiikawacoding.core.AiCodeGeneratorFacade;
import com.chiikawa.chiikawacoding.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("潘凯闻爱李美慧网站", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeFlux() {
        Flux<String> flux = aiCodeGeneratorFacade.generateAndSaveCodeStream("做爱记录网站，包括做爱时间、做爱体验、高潮次数等", CodeGenTypeEnum.MULTI_FILE);
        // 把flux的结果流式打印
        flux.subscribe(System.out::println);
    }

}
