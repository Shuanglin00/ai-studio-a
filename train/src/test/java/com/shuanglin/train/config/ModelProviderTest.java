package com.shuanglin.train.config;

import com.shuanglin.train.TrainApplication;
import com.shuanglin.train.model.impl.HuggingfaceTextModel;
import com.shuanglin.train.model.type.EmbeddingModel;
import com.shuanglin.train.model.type.TextModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelProvider 测试
 */
@SpringBootTest(classes = TrainApplication.class)
@TestPropertySource(properties = {
        "milvus.enable=false",
        "spring.autoconfigure.exclude=org.dromara.milvus.plus.service.MilvusInit",
        "train.model.cache-enabled=false"
})
public class ModelProviderTest {

    @Autowired
    private ModelProvider modelProvider;

    @Test
    void testGetDefaultTextModel() {
        TextModel textModel = modelProvider.getTextModel();
        assertNotNull(textModel);
    }

    @Test
    void testGetDefaultEmbeddingModel() {
        EmbeddingModel embeddingModel = modelProvider.getEmbeddingModel();
        assertNotNull(embeddingModel);
    }

    @Test
    void testGetTextModelByBeanName() {
        Optional<TextModel> model = modelProvider.getTextModel("huggingfaceTextModel");
        assertTrue(model.isPresent());
        assertInstanceOf(HuggingfaceTextModel.class, model.get());
    }

    @Test
    void testGetTextModelByInvalidBeanName() {
        Optional<TextModel> model = modelProvider.getTextModel("nonExistentModel");
        assertFalse(model.isPresent());
    }

    @Test
    void testGetModelGeneric() {
        var model = modelProvider.getModel("huggingfaceTextModel", TextModel.class);
        assertTrue(model.isPresent());
        assertInstanceOf(TextModel.class, model.get());
    }

    @Test
    void testGetModelWithWrongType() {
        // Requesting TextModel by embedding bean name should fail
        var model = modelProvider.getModel("huggingfaceEmbeddingModel", TextModel.class);
        assertFalse(model.isPresent());
    }
}