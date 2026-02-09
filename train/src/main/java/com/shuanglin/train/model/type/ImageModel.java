package com.shuanglin.train.model.type;

import com.shuanglin.train.model.base.DjlModel;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * 图像生成模型接口
 * 用于文生图场景
 */
public interface ImageModel extends DjlModel {

    /**
     * 根据提示词生成图像
     * @param prompt 提示词
     * @return 生成的图像
     */
    BufferedImage generate(String prompt);

    /**
     * 根据提示词生成图像并保存
     * @param prompt 提示词
     * @param outputPath 输出路径
     * @return 生成图像的路径
     */
    Path generateAndSave(String prompt, Path outputPath);

    /**
     * 根据提示词和负向提示词生成图像
     * @param prompt 正向提示词
     * @param negativePrompt 负向提示词
     * @return 生成的图像
     */
    BufferedImage generate(String prompt, String negativePrompt);

    /**
     * 获取支持的最大图像分辨率
     */
    default int[] getMaxResolution() {
        return new int[]{1024, 1024};
    }

    /**
     * 获取默认图像尺寸
     */
    default int[] getDefaultSize() {
        return new int[]{512, 512};
    }
}
