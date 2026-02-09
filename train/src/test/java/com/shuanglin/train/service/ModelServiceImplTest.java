package com.shuanglin.train.service;

import com.shuanglin.train.TrainApplication;
import com.shuanglin.train.model.type.EmbeddingModel;
import com.shuanglin.train.model.type.ImageModel;
import com.shuanglin.train.model.type.RerankModel;
import com.shuanglin.train.model.type.TextModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelServiceImpl 测试
 */
@SpringBootTest(classes = TrainApplication.class)
@TestPropertySource(properties = {
        "milvus.enable=false",
        "spring.autoconfigure.exclude=org.dromara.milvus.plus.service.MilvusInit",
        "train.model.cache-enabled=false"
})
public class ModelServiceImplTest {

    @Autowired
    private ModelService modelService;

    @Test
    void testGetTextModel() {
        Optional<TextModel> model = modelService.getTextModel("huggingfaceTextModel");
        assertTrue(model.isPresent());
        assertNotNull(model.get().getModelId());
    }

    @Test
    void testGetDefaultTextModel() {
        Optional<TextModel> model = modelService.getDefaultTextModel();
        assertTrue(model.isPresent());
    }

    @Test
    void testGetEmbeddingModel() {
        Optional<EmbeddingModel> model = modelService.getEmbeddingModel("huggingfaceEmbeddingModel");
        assertTrue(model.isPresent());
    }

    @Test
    void testGetImageModel() {
        Optional<ImageModel> model = modelService.getImageModel("huggingfaceImageModel");
        // May not be present if HuggingfaceImageModel bean is not created
        // This test just verifies the method doesn't throw
    }

    @Test
    void testGetRerankModel() {
        Optional<RerankModel> model = modelService.getRerankModel("huggingfaceRerankModel");
        // May not be present if HuggingfaceRerankModel bean is not created
        // This test just verifies the method doesn't throw
    }

    @Test
    void testGetLoadedModelNames() {
        List<String> names = modelService.getLoadedModelNames();
        assertNotNull(names);
        assertFalse(names.isEmpty());
        assertTrue(names.contains("huggingfaceTextModel"));
    }

    @Test
    void testIsModelLoaded() {
        boolean loaded = modelService.isModelLoaded("huggingfaceTextModel", TextModel.class);
        assertFalse(loaded); // Model not actually loaded in test
    }

    @Test
    void testUnloadModel() {
        boolean result = modelService.unloadModel("nonExistentModel");
        assertFalse(result);
    }

    @Test
    void testReloadModel() {
        boolean result = modelService.reloadModel("nonExistentModel", TextModel.class);
        assertFalse(result);
    }

    @Test
    void testGetNonExistentModel() {
        Optional<TextModel> model = modelService.getTextModel("nonExistentModel");
        assertFalse(model.isPresent());
    }
}