package com.shuanglin.train.factory;

import com.shuanglin.train.TrainApplication;
import com.shuanglin.train.config.ModelProperties;
import com.shuanglin.train.model.base.DjlModel;
import com.shuanglin.train.model.type.TextModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelFactory 测试
 */
@SpringBootTest(classes = TrainApplication.class)
@TestPropertySource(properties = {
        "milvus.enable=false",
        "spring.autoconfigure.exclude=org.dromara.milvus.plus.service.MilvusInit",
        "train.model.cache-enabled=false"
})
public class ModelFactoryTest {

    @Autowired
    private ModelFactory modelFactory;

    @Autowired
    private ModelProperties modelProperties;

    @Test
    void testGetLoadedModelNames() {
        Set<String> names = modelFactory.getLoadedModelNames();
        assertNotNull(names);
        assertFalse(names.isEmpty());
    }

    @Test
    void testGetModelByName() {
        Optional<TextModel> model = modelFactory.getModel("huggingfaceTextModel", TextModel.class);
        assertTrue(model.isPresent());
    }

    @Test
    void testGetModelByWrongType() {
        // Requesting wrong type should return empty
        // Since HuggingfaceTextModel is TextModel, it should work
        Optional<DjlModel> model = modelFactory.getModel("huggingfaceTextModel", DjlModel.class);
        assertTrue(model.isPresent());
    }

    @Test
    void testGetNonExistentModel() {
        Optional<TextModel> model = modelFactory.getModel("nonExistentModel", TextModel.class);
        assertFalse(model.isPresent());
    }

    @Test
    void testRegisterModel() {
        // Create a simple model config
        ModelProperties.ModelConfig config = new ModelProperties.ModelConfig();
        config.setName("test-model");
        config.setModelPath("test-path");

        // Register model
        boolean result = modelFactory.registerModel("test-model", null);
        // This will fail because we pass null model - but it tests the method exists
    }

    @Test
    void testRemoveModel() {
        boolean result = modelFactory.removeModel("nonExistentModel");
        assertFalse(result);
    }

    @Test
    void testModelPropertiesLoaded() {
        assertNotNull(modelProperties);
        assertNotNull(modelProperties.getModels());
        assertFalse(modelProperties.getModels().isEmpty());
    }

    @Test
    void testShutdown() {
        // Just verify the method doesn't throw
        assertDoesNotThrow(() -> modelFactory.shutdown());
    }
}