package com.shuanglin.train.factory;

import com.shuanglin.train.config.ModelProperties;
import com.shuanglin.train.model.base.DjlModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型工厂类
 * 提供模型实例管理和获取服务
 *
 * 使用方式:
 * <pre>
 * // 注入默认模型
 * @Autowired
 * private TextModel textModel;
 *
 * // 注入指定模型
 * @Autowired
 * @Qualifier("huggingfaceTextModel")
 * private HuggingfaceTextModel huggingfaceModel;
 *
 * // 使用工厂获取
 * @Autowired
 * private ModelFactory modelFactory;
 * private void someMethod() {
 *     TextModel model = modelFactory.getDefaultTextModel();
 * }
 * </pre>
 */
@Slf4j
@Component
public class ModelFactory {

    private final ModelProperties modelProperties;
    private final Map<String, DjlModel> modelCache = new ConcurrentHashMap<>();

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    public ModelFactory(ModelProperties modelProperties) {
        this.modelProperties = modelProperties;
    }

    @PostConstruct
    public void init() {
        log.info("模型工厂初始化完成");
        log.info("可通过 @Autowired + @Qualifier 注入具体模型");
        log.info("或直接注入 TextModel/EmbeddingModel 等接口（使用 @Primary 的实现）");
    }

    /**
     * 获取指定名称的模型
     */
    @SuppressWarnings("unchecked")
    public <T extends DjlModel> Optional<T> getModel(String modelName, Class<T> modelType) {
        DjlModel model = modelCache.get(modelName);
        if (model == null) {
            log.warn("模型未找到: {}", modelName);
            return Optional.empty();
        }
        if (modelType.isInstance(model)) {
            return Optional.of((T) model);
        }
        return Optional.empty();
    }

    /**
     * 动态注册模型
     */
    public boolean registerModel(String name, DjlModel model) {
        if (modelCache.size() >= modelProperties.getMaxModels()) {
            log.warn("已达到最大模型数量限制: {}", modelProperties.getMaxModels());
            return false;
        }
        modelCache.put(name, model);
        log.info("动态注册模型: {}", name);
        return true;
    }

    /**
     * 卸载并移除模型
     */
    public boolean removeModel(String name) {
        DjlModel model = modelCache.remove(name);
        if (model != null) {
            model.unload();
            log.info("已移除模型: {}", name);
            return true;
        }
        return false;
    }

    /**
     * 获取所有已加载的模型名称
     */
    public Set<String> getLoadedModelNames() {
        return modelCache.keySet();
    }

    @PreDestroy
    public void shutdown() {
        log.info("正在卸载所有模型...");
        modelCache.values().forEach(DjlModel::unload);
        modelCache.clear();
    }
}
