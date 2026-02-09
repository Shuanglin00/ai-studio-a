import com.shuanglin.ChatStart;
import com.shuanglin.bot.langchain4j.assistant.DecomposeAssistant;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ChatStart.class)
public class ChapterOverviewTest {

    @Resource
    private DecomposeAssistant decomposeAssistant;

    private static final String NEO4J_URI = "bolt://8.138.204.38:7687";
    private static final String NEO4J_USER = "neo4j";
    private static final String NEO4J_PASSWORD = "password";

    /**
     * 获取前20章的详细章节内容，同时输出查询到的Cypher内容
     */
    @Test
    public void testGenerateFirst20ChaptersDetailedContent() {
        // 创建Neo4j驱动
        Driver driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));

        try {
            System.out.println("开始生成前20章的详细内容概述...");
            
            // 生成前20章的概述
            for (int chapterIndex = 1; chapterIndex <= 20; chapterIndex++) {
                System.out.println("\n========== 第 " + chapterIndex + " 章 ==========");
                
                // 查询指定章节的知识图谱内容
                String chapterData = queryChapterDataAsText(driver, chapterIndex);
                
                // 打印查询到的数据
//                System.out.println("查询到的知识图谱数据:");
//                System.out.println(chapterData);
                
                // 使用LLM生成自然语言章节概述
                String detailedOverview = generateNaturalLanguageOverview(chapterData, chapterIndex);
                
                // 打印LLM生成的概述
                System.out.println("\n章节概述:");
                System.out.println(detailedOverview);
                
                // 添加分隔符
                System.out.println("\n" + "=".repeat(60));
            }
            
            System.out.println("\n✅ 前20章内容生成完成！");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭驱动
            driver.close();
        }
    }

    /**
     * 使用Cypher查询指定章节的知识图谱内容，并以文本形式返回
     * 
     * @param driver Neo4j驱动
     * @param chapterIndex 章节索引
     * @return 章节数据的文本表示
     */
    private String queryChapterDataAsText(Driver driver, int chapterIndex) {
        StringBuilder result = new StringBuilder();
        
        try (Session session = driver.session()) {
            result.append("第 ").append(chapterIndex).append(" 章知识图谱数据:\n\n");
            
            // 查询章节中的事件
            Result eventResult = session.run(
                "MATCH (e:Event) WHERE e.chapterIndex = $chapterIndex RETURN e ORDER BY e.chapterIndex",
                Values.parameters("chapterIndex", chapterIndex)
            );
            
            result.append("## 事件 (Events)\n");
            while (eventResult.hasNext()) {
                Record record = eventResult.next();
                result.append("- ").append(record.get("e").asNode().asMap()).append("\n");
            }
            result.append("\n");
            
            // 查询章节中的实体
            Result entityResult = session.run(
                "MATCH (ent:Entity) WHERE ent.createdAt = $chapterIndex RETURN ent LIMIT 20",
                Values.parameters("chapterIndex", chapterIndex)
            );
            
            result.append("## 实体 (Entities)\n");
            while (entityResult.hasNext()) {
                Record record = entityResult.next();
                result.append("- ").append(record.get("ent").asNode().asMap()).append("\n");
            }
            result.append("\n");
            
            // 查询章节中涉及的状态变化
            Result stateResult = session.run(
                "MATCH (s:State) WHERE s.valid_from_chapter = $chapterIndex RETURN s LIMIT 20",
                Values.parameters("chapterIndex", chapterIndex)
            );
            
            result.append("## 状态变化 (States)\n");
            while (stateResult.hasNext()) {
                Record record = stateResult.next();
                result.append("- ").append(record.get("s").asNode().asMap()).append("\n");
            }
            result.append("\n");
            
            // 查询实体间的关系
            Result relationResult = session.run(
                "MATCH (e:Event)-[r]->(n) WHERE e.chapterIndex = $chapterIndex RETURN e, r, n LIMIT 20",
                Values.parameters("chapterIndex", chapterIndex)
            );
            
            result.append("## 关系 (Relationships)\n");
            while (relationResult.hasNext()) {
                Record record = relationResult.next();
                var eventNode = record.get("e").asNode();
                var rel = record.get("r").asRelationship();
                var node = record.get("n").asNode();
                
                String eventDesc = eventNode.get("description").asString("未知事件");
                String nodeName = node.get("name").asString("未知实体");
                
                result.append("- ").append(eventDesc)
                      .append(" --[").append(rel.type()).append("]--> ")
                      .append(nodeName).append("\n");
            }
            result.append("\n");
            
        } catch (Exception e) {
            System.err.println("查询章节数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result.toString();
    }

    /**
     * 使用LLM将知识图谱数据转换为自然语言概述
     * 
     * @param chapterData 章节数据的文本表示
     * @param chapterIndex 章节索引
     * @return 自然语言形式的章节概述
     */
    private String generateNaturalLanguageOverview(String chapterData, int chapterIndex) {
        try {
            // 构造提示词
            String prompt = """
                你是一个小说内容分析专家，请将以下知识图谱数据转换为自然语言形式的章节概述：
                
                章节索引: 第 %d 章
                
                %s
                
                请基于以上知识图谱数据，生成一段流畅的章节概述文本，要求：
                1. 用自然语言描述本章的主要情节发展
                2. 突出重要角色和事件
                3. 描述关键的状态变化和关系建立
                4. 保持故事的连贯性和逻辑性
                5. 使用生动的中文表达
                6. 不要添加任何解释或前缀，直接输出章节概述
                
                章节概述：
                """.formatted(chapterIndex, chapterData);
            
            // 调用LLM生成概述
            System.out.println("正在生成第 " + chapterIndex + " 章概述...");
            return decomposeAssistant.generateCypher(prompt);
        } catch (Exception e) {
            System.err.println("使用LLM生成章节概述时出错: " + e.getMessage());
            e.printStackTrace();
            return "无法生成详细概述。";
        }
    }
}