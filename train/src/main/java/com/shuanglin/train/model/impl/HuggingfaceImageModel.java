package com.shuanglin.train.model.impl;

import com.shuanglin.train.config.ModelProperties;
import com.shuanglin.train.model.framework.AbstractHuggingfaceModel;
import com.shuanglin.train.model.type.ImageModel;
import com.shuanglin.train.model.type.ModelType;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * HuggingFace 图像生成模型实现
 * 架构层级: model:stable-diffusion-huggingface -> type:imageModel -> framework:huggingface -> impl:HuggingfaceImageModel
 */
@Slf4j
public class HuggingfaceImageModel extends AbstractHuggingfaceModel implements ImageModel {

    public HuggingfaceImageModel(ModelProperties.ModelConfig config) {
        super(config);
        if (config.getType() != ModelType.IMAGE_MODEL) {
            throw new IllegalArgumentException("模型类型不匹配，期望 IMAGE_MODEL");
        }
    }

    @Override
    public ModelType getModelType() {
        return ModelType.IMAGE_MODEL;
    }

    @Override
    public void load() {
        log.info("加载 HuggingFace 图像模型: {}", getModelId());
        // TODO: 使用 DJL 加载 HuggingFace 图像生成模型 (Stable Diffusion 等)
        this.loaded = true;
    }

    @Override
    public void unload() {
        log.info("卸载 HuggingFace 图像模型: {}", getModelId());
        // TODO: 释放模型资源
        this.loaded = false;
    }

    @Override
    public BufferedImage generate(String prompt) {
        ensureLoaded();
        log.debug("生成图像，prompt: {}", prompt);
        // TODO: 调用 DJL 生成图像
        return null;
    }

    @Override
    public Path generateAndSave(String prompt, Path outputPath) {
        ensureLoaded();
        log.debug("生成图像并保存: {}", outputPath);
        // TODO: 生成并保存图像
        return outputPath;
    }

    @Override
    public BufferedImage generate(String prompt, String negativePrompt) {
        ensureLoaded();
        log.debug("生成图像，正向: {}, 负向: {}", prompt, negativePrompt);
        // TODO: 带负向提示词生成
        return generate(prompt);
    }

    private void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }
}
