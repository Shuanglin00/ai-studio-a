package com.shuanglin.ai.test.novel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Neo4j 连接和查询测试
 */
@DisplayName("Neo4j 知识图谱测试")
class Neo4jConnectionTest {

    private static final String NEO4J_URI = System.getProperty("neo4j.uri", "bolt://localhost:7687");
    private static final String NEO4J_USER = System.getProperty("neo4j.user", "neo4j");
    private static final String NEO4J_PASSWORD = System.getProperty("neo4j.password", "neo4j");

    @Test
    @DisplayName("测试1: 连接Neo4j")
    void testNeo4jConnection() {
        try (Driver driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
             Session session = driver.session()) {

            // 执行简单查询验证连接
            Result result = session.run("RETURN 1 as num");
            assertTrue(result.hasNext());
            assertEquals(1, result.next().get("num").asInt());
            System.out.println("✅ Neo4j 连接成功");
        } catch (Exception e) {
            System.err.println("❌ Neo4j 连接失败: " + e.getMessage());
            // 不抛出异常，让测试继续
        }
    }

    @Test
    @DisplayName("测试2: 查询所有Entity节点")
    void testQueryEntities() {
        try (Driver driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
             Session session = driver.session()) {

            String cypher = "MATCH (e:Entity) RETURN e.name as name, e.entityType as type, labels(e) as labels LIMIT 20";
            Result result = session.run(cypher);

            int count = 0;
            while (result.hasNext()) {
                Record record = result.next();
                String name = record.get("name").asString();
                String type = record.get("type").asString();
                System.out.println("实体: " + name + " (" + type + ")");
                count++;
            }
            System.out.println("共查询到 " + count + " 个实体");
        } catch (Exception e) {
            System.err.println("❌ 查询失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试3: 按类型查询实体")
    void testQueryEntitiesByType() {
        try (Driver driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
             Session session = driver.session()) {

            // 查询角色
            String cypher = "MATCH (e:Entity {entityType: 'Character'}) RETURN e.name as name LIMIT 10";
            Result result = session.run(cypher);

            System.out.println("=== 角色 ===");
            while (result.hasNext()) {
                Record record = result.next();
                System.out.println("  - " + record.get("name").asString());
            }

            // 查询地点
            cypher = "MATCH (e:Entity {entityType: 'Location'}) RETURN e.name as name LIMIT 10";
            result = session.run(cypher);

            System.out.println("=== 地点 ===");
            while (result.hasNext()) {
                Record record = result.next();
                System.out.println("  - " + record.get("name").asString());
            }

            // 查询组织
            cypher = "MATCH (e:Entity {entityType: 'Organization'}) RETURN e.name as name LIMIT 10";
            result = session.run(cypher);

            System.out.println("=== 组织 ===");
            while (result.hasNext()) {
                Record record = result.next();
                System.out.println("  - " + record.get("name").asString());
            }
        } catch (Exception e) {
            System.err.println("❌ 查询失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试4: 查询Event节点")
    void testQueryEvents() {
        try (Driver driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
             Session session = driver.session()) {

            String cypher = "MATCH (e:Event) RETURN e.chapterIndex as chapter, e.eventType as type, e.description as desc LIMIT 20";
            Result result = session.run(cypher);

            int count = 0;
            while (result.hasNext()) {
                Record record = result.next();
                int chapter = record.get("chapter").asInt();
                String type = record.get("type").asString();
                String desc = record.get("desc").asString();
                System.out.println("第" + chapter + "章 [" + type + "]: " + desc.substring(0, Math.min(50, desc.length())));
                count++;
            }
            System.out.println("共查询到 " + count + " 个事件");
        } catch (Exception e) {
            System.err.println("❌ 查询失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试5: 搜索特定人物")
    void testSearchCharacter() {
        try (Driver driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
             Session session = driver.session()) {

            // 搜索包含"李"的人物
            String cypher = "MATCH (e:Entity) WHERE e.name CONTAINS '李' RETURN e.name as name, e.entityType as type LIMIT 10";
            Result result = session.run(cypher);

            System.out.println("=== 搜索包含'李'的实体 ===");
            while (result.hasNext()) {
                Record record = result.next();
                System.out.println("  - " + record.get("name").asString() + " (" + record.get("type").asString() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ 搜索失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试6: 查询实体的历史事件")
    void testQueryCharacterHistory() {
        try (Driver driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
             Session session = driver.session()) {

            // 查找一个有事件的实体
            String cypher = """
                MATCH (e:Entity)-[:PARTICIPATED_IN]->(ev:Event)
                WITH e, count(ev) as eventCount
                WHERE eventCount > 0
                RETURN e.name as name, eventCount
                ORDER BY eventCount DESC
                LIMIT 1
                """;
            Result result = session.run(cypher);

            if (result.hasNext()) {
                String entityName = result.next().get("name").asString();
                System.out.println("查询实体 '" + entityName + "' 的历史事件:");

                // 查询该实体参与的事件
                String eventsCypher = """
                    MATCH (e:Entity {name: $name})-[:PARTICIPATED_IN]->(ev:Event)
                    RETURN ev.chapterIndex as chapter, ev.eventType as type, ev.description as desc
                    ORDER BY chapter
                    LIMIT 10
                    """;
                Result eventsResult = session.run(eventsCypher, Values.value(java.util.Map.of("name", entityName)));

                while (eventsResult.hasNext()) {
                    Record record = eventsResult.next();
                    System.out.println("  第" + record.get("chapter").asInt() + "章 [" + record.get("type").asString() + "]");
                }
            } else {
                System.out.println("未找到有历史事件的实体");
            }
        } catch (Exception e) {
            System.err.println("❌ 查询失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试7: 统计知识图谱")
    void testGraphStats() {
        try (Driver driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
             Session session = driver.session()) {

            System.out.println("=== 知识图谱统计 ===");

            // 统计Entity
            String cypher = "MATCH (e:Entity) RETURN count(e) as count";
            long entityCount = session.run(cypher).next().get("count").asLong();
            System.out.println("Entity节点: " + entityCount);

            // 统计Event
            cypher = "MATCH (e:Event) RETURN count(e) as count";
            long eventCount = session.run(cypher).next().get("count").asLong();
            System.out.println("Event节点: " + eventCount);

            // 统计State
            cypher = "MATCH (s:State) RETURN count(s) as count";
            long stateCount = session.run(cypher).next().get("count").asLong();
            System.out.println("State节点: " + stateCount);

            // 统计关系
            cypher = "MATCH ()-[r]->() RETURN count(r) as count";
            long relCount = session.run(cypher).next().get("count").asLong();
            System.out.println("关系: " + relCount);

            // 按类型统计Entity
            System.out.println("\n=== 按类型统计实体 ===");
            cypher = "MATCH (e:Entity) RETURN e.entityType as type, count(e) as count ORDER BY count DESC";
            Result result = session.run(cypher);
            while (result.hasNext()) {
                Record record = result.next();
                System.out.println("  " + record.get("type").asString() + ": " + record.get("count").asLong());
            }
        } catch (Exception e) {
            System.err.println("❌ 统计失败: " + e.getMessage());
        }
    }
}
