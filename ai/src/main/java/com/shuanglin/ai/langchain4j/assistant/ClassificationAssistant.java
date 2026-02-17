package com.shuanglin.ai.langchain4j.assistant;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 消息分类助手
 * 使用LLM对群聊消息进行自动分类
 */
public interface ClassificationAssistant {

    /**
     * 对消息批次进行分类
     *
     * @param categories        可选分类列表
     * @param triggerType       触发消息类型
     * @param triggerContent    触发消息内容
     * @param messageCount      消息数量
     * @param messages          消息内容
     * @return 分类结果JSON
     */
    @UserMessage("""
        你是一名专业的群聊消息内容分类专家。请分析以下消息批次，判断其内容类型。

        ## 可选分类
        {{categories}}

        ## 分类标准
        1. 弔图(MEME)：搞笑图片、表情包引发的"哈哈哈哈"、"笑死"等反应
        2. 地狱笑话(HELL_JOKE)：黑色幽默、涉及敏感话题（死亡、灾难、疾病等）的笑话
        3. 地域黑(REGIONAL_BLACK)：针对特定地区、省份、城市的负面言论或刻板印象
        4. 政治敏感(POLITICAL)：涉及政治人物、政策、敏感历史事件的讨论
        5. NSFW：不适宜工作场所的内容（色情、暴力等）
        6. 正常(NORMAL)：普通聊天内容
        7. 刷屏(SPAM)：无意义的重复内容、纯表情刷屏
        8. 广告(AD)：商业广告、推广信息

        ## 输入格式
        触发消息类型：{{triggerType}}
        触发消息内容：{{triggerContent}}

        后续消息（共{{messageCount}}条）：
        {{messages}}

        ## 输出要求
        请严格按照以下JSON格式输出，不要包含任何其他内容：
        {
            "primaryCategory": "主分类编码",
            "subCategories": ["子分类1", "子分类2"],
            "confidence": 0.95,
            "reasoning": "分类理由，基于消息内容分析",
            "keyIndicators": ["关键指标1", "关键指标2"]
        }
        """)
    String classify(
            @V("categories") String categories,
            @V("triggerType") String triggerType,
            @V("triggerContent") String triggerContent,
            @V("messageCount") int messageCount,
            @V("messages") String messages
    );
}
