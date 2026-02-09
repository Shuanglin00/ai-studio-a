package com.shuanglin.train.service;

import com.shuanglin.train.config.ModelProperties;
import com.shuanglin.train.factory.ModelFactory;
import com.shuanglin.train.model.base.DjlModel;
import com.shuanglin.train.model.type.EmbeddingModel;
import com.shuanglin.train.model.type.ImageModel;
import com.shuanglin.train.model.type.RerankModel;
import com.shuanglin.train.model.type.TextModel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * DJL 模型服务实现
 * AI 模块通过注入此服务访问模型
 */
@Slf4j
@Service
@Setter(onMethod_ = @Autowired)
public class ModelServiceImpl implements ModelService {
    private final ModelFactory modelFactory;
    private final ModelProperties modelProperties;

    public ModelServiceImpl(ModelFactory modelFactory, ModelProperties modelProperties) {
        this.modelFactory = modelFactory;
        this.modelProperties = modelProperties;
    }

    @Override
    public Optional<TextModel> getTextModel(String modelName) {
        return modelFactory.getModel(modelName, TextModel.class);
    }

    @Override
    public Optional<TextModel> getDefaultTextModel() {
        // 查找第一个可用的 TextModel
        return modelFactory.getLoadedModelNames().stream()
                .map(name -> modelFactory.getModel(name, TextModel.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public Optional<EmbeddingModel> getEmbeddingModel(String modelName) {
        return modelFactory.getModel(modelName, EmbeddingModel.class);
    }

    @Override
    public Optional<EmbeddingModel> getDefaultEmbeddingModel() {
        return modelFactory.getLoadedModelNames().stream()
                .map(name -> modelFactory.getModel(name, EmbeddingModel.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public Optional<ImageModel> getImageModel(String modelName) {
        return modelFactory.getModel(modelName, ImageModel.class);
    }

    @Override
    public Optional<ImageModel> getDefaultImageModel() {
        return modelFactory.getLoadedModelNames().stream()
                .map(name -> modelFactory.getModel(name, ImageModel.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public Optional<RerankModel> getRerankModel(String modelName) {
        return modelFactory.getModel(modelName, RerankModel.class);
    }

    @Override
    public Optional<RerankModel> getDefaultRerankModel() {
        return modelFactory.getLoadedModelNames().stream()
                .map(name -> modelFactory.getModel(name, RerankModel.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DjlModel> Optional<T> getModel(String modelName, Class<T> modelType) {
        return modelFactory.getModel(modelName, modelType);
    }

    @Override
    public List<String> getLoadedModelNames() {
        return List.copyOf(modelFactory.getLoadedModelNames());
    }

    @Override
    public boolean isModelLoaded(String modelName, Class<? extends DjlModel> modelType) {
        return modelFactory.getModel(modelName, modelType)
                .map(DjlModel::isLoaded)
                .orElse(false);
    }

    @Override
    public boolean reloadModel(String modelName, Class<? extends DjlModel> modelType) {
//        Optional<DjlModel> modelOpt = modelFactory.getModel(modelName, modelType);
//        if (modelOpt.isPresent()) {
//            DjlModel model = modelOpt.get();
//            model.unload();
//            model.load();
//            log.info("模型重新加载成功: {}", modelName);
//            return true;
//        }
        log.warn("模型不存在，无法重新加载: {}", modelName);
        return false;
    }

    @Override
    public boolean unloadModel(String modelName) {
        return modelFactory.removeModel(modelName);
    }
}
