# ai-studio
base langchain4j + local lmstudio +springboot

# 迭代
## V1

> 基于会话

## V2

> 数据增加无会话

## V3

> 将数据进行无会话和有会话存储

## V4

> 多步推理RAG（Multi-step RAG）
> 不需要历史会话，依赖RAG子查询检索知识库，构建prompt

# 支持的模型

## Ollama
- 默认使用Ollama模型进行聊天

## Gemini
- Google Gemini模型支持

## Qwen
- 阿里通义千问模型支持

## MiniMax
- 新增MiniMax模型支持
- 模型名称: MiniMax-M2
- API地址: https://api.minimaxi.com/v1
- 配置方法: 在application.yaml中设置langchain4j.models.minimax.apiKey