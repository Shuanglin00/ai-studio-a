package com.shuanglin;

import com.shuanglin.train.TrainApplication;
import com.shuanglin.train.config.ModelAutoConfiguration;
import com.shuanglin.train.config.ModelProperties;
import com.shuanglin.train.model.impl.HuggingfaceTextModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@Import({ModelAutoConfiguration.class, ModelProperties.class})
@SpringBootTest(classes = TrainApplication.class)
public class DJLModelTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private HuggingfaceTextModel huggingfaceTextModel;

    @Test
    public void testGenerate() {
        // Debug: print all bean names
        System.out.println("=== Available Beans ===");
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String name : beanNames) {
            if (name.toLowerCase().contains("model") || name.toLowerCase().contains("hugging")) {
                System.out.println(name);
            }
        }

        // Check if bean exists
        assertNotNull(huggingfaceTextModel, "huggingfaceTextModel should not be null");
        assertNotNull(applicationContext.getBean("huggingfaceTextModel"), "huggingfaceTextModel bean should exist");

        String prompt = "请生成一个关于机器学习的段落";
        String result = huggingfaceTextModel.generate(prompt);
        System.out.println("Result: " + result);
    }
}
