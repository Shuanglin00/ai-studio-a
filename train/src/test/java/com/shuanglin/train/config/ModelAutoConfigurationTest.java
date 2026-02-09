package com.shuanglin.train.config;

import com.shuanglin.train.TrainApplication;
import com.shuanglin.train.model.impl.HuggingfaceTextModel;
import com.shuanglin.train.model.type.TextModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelAutoConfiguration 测试
 */
@SpringBootTest(classes = TrainApplication.class)
@TestPropertySource(properties = {
        "milvus.enable=false",
        "spring.autoconfigure.exclude=org.dromara.milvus.plus.service.MilvusInit",
        "train.model.cache-enabled=false"
})
public class ModelAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private HuggingfaceTextModel huggingfaceTextModel;

    @Autowired
    private TextModel textModel;

    @Test
    void testHuggingfaceTextModelBeanExists() {
        assertNotNull(huggingfaceTextModel);
        assertTrue(applicationContext.containsBean("huggingfaceTextModel"));
    }

    @Test
    void testTextModelPrimaryBeanExists() {
        assertNotNull(textModel);
        assertSame(huggingfaceTextModel, textModel);
    }

    @Test
    void testModelConfiguration() {
        assertNotNull(huggingfaceTextModel.getModelId());
        assertNotNull(huggingfaceTextModel.getModelType());
    }
}