import com.shuanglin.bot.service.GraphService;
import com.shuanglin.bot.utils.FileReadUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GraphService章节级处理单元测试
 * 测试核心方法的正确性
 */
public class GraphServiceChapterLevelTest {

    /**
     * 测试aggregateParagraphs方法
     * 验证段落聚合逻辑
     */
    @Test
    public void testAggregateParagraphs() {
        GraphService graphService = new GraphService();
        
        // 测试空列表
        List<String> emptyList = Arrays.asList();
        String result1 = invokeAggregateParagraphs(graphService, emptyList);
        assertEquals("", result1, "空列表应返回空字符串");
        
        // 测试包含空元素的列表
        List<String> listWithEmpty = Arrays.asList("段落1", "", null, "段落2");
        String result2 = invokeAggregateParagraphs(graphService, listWithEmpty);
        assertEquals("段落1\n段落2", result2, "应过滤空元素并用换行符连接");
        
        // 测试正常列表
        List<String> normalList = Arrays.asList("第一段", "第二段", "第三段");
        String result3 = invokeAggregateParagraphs(graphService, normalList);
        assertEquals("第一段\n第二段\n第三段", result3, "应正确聚合段落");
    }
    
    /**
     * 测试validate方法
     * 验证Cypher验证逻辑
     */
    @Test
    public void testValidate() {
        GraphService graphService = new GraphService();
        
        // 测试空字符串
        assertFalse(invokeValidate(graphService, ""), "空字符串应验证失败");
        assertFalse(invokeValidate(graphService, null), "null应验证失败");
        
        // 测试包含paragraphIndex的Cypher（应拒绝非null值）
        String cypherWithParagraphIndex = "CREATE (e:Event {paragraphIndex: 1})";
        assertFalse(invokeValidate(graphService, cypherWithParagraphIndex), 
                "包含非null的paragraphIndex应验证失败");
        
        // 测试包含paragraphIndex: null的Cypher（应通过）
        String cypherWithNullParagraphIndex = "CREATE (e:Event {paragraphIndex: null})";
        assertTrue(invokeValidate(graphService, cypherWithNullParagraphIndex), 
                "包含paragraphIndex: null应验证通过");
        
        // 测试chapterIndex为正整数的Cypher（应通过）
        String validCypher = "CREATE (e:Event {chapterIndex: 5})";
        assertTrue(invokeValidate(graphService, validCypher), "包含正整数chapterIndex的Cypher应验证通过");
        
        // 测试chapterIndex为负数的Cypher（应拒绝）
        String invalidCypher = "CREATE (e:Event {chapterIndex: -1})";
        assertFalse(invokeValidate(graphService, invalidCypher), "包含负数chapterIndex的Cypher应验证失败");
    }
    
    // ============ 反射调用私有方法的辅助方法 ============
    
    private String invokeAggregateParagraphs(GraphService service, List<String> contentList) {
        try {
            var method = GraphService.class.getDeclaredMethod("aggregateParagraphs", List.class);
            method.setAccessible(true);
            return (String) method.invoke(service, contentList);
        } catch (Exception e) {
            throw new RuntimeException("反射调用失败", e);
        }
    }
    
    private boolean invokeValidate(GraphService service, String cypher) {
        try {
            var method = GraphService.class.getDeclaredMethod("validate", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(service, cypher);
        } catch (Exception e) {
            throw new RuntimeException("反射调用失败", e);
        }
    }
}