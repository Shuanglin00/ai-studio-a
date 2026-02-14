# 多阶段构建：先构建JAR，再运行
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制pom文件和源码
COPY pom.xml .
COPY common common/
COPY dbModel dbModel/
COPY ai ai/
COPY bot bot/

# 构建jar包 (使用sit profile, 跳过测试)
RUN mvn clean package -DskipTests -Psit -pl bot -am

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装必要工具 (docker save/load需要)
RUN apk add --no-cache docker bash

# 创建非root用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 复制jar包
COPY --from=builder /build/bot/target/bot-1.0.jar app.jar

# 复制配置文件 (运行时从外部挂载)
RUN mkdir -p /app/config && chown -R appuser:appgroup /app

# 设置权限
USER appuser

# JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.config.additional-location=file:/app/config/"]
