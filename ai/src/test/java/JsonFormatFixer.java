/**
 * JSON格式修复工具类
 * 处理LLM输出的各种JSON格式问题
 */
public class JsonFormatFixer {
    
    /**
     * 修复JSON格式问题 - 综合解决方案
     * 
     * 处理以下问题：
     * 1. 中文标点符号（""''：，等）
     * 2. 字符串值中的未转义引号
     * 3. 全角括号
     * 4. 已转义字符的正确处理
     */
    public static String fix(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        
        // 第一步：替换中文标点符号为英文标点
        json = replacePunctuation(json);
        
        // 第二步：处理字符串值中的未转义引号
        json = escapeQuotesInStrings(json);
        
        return json;
    }
    
    /**
     * 替换中文标点符号为英文标点
     */
    private static String replacePunctuation(String json) {
        return json
            .replace("“", "\"")
            .replace("”", "\"")
            .replace("‘", "'")
            .replace("’", "'")
            .replace("：", ":")
            .replace("，", ",")
            .replace("｛", "{")
            .replace("｝", "}")
            .replace("［", "[")
            .replace("］", "]");
    }
    
    /**
     * 转义JSON字符串值中的引号
     * 
     * 核心逻辑：
     * 1. 遇到引号时，检查后续第一个非空白字符
     * 2. 如果是结构字符(, : } ])，说明是字符串结束
     * 3. 否则是内容中的引号，需要转义
     * 4. 正确处理已转义的字符
     */
    private static String escapeQuotesInStrings(String json) {
        StringBuilder result = new StringBuilder(json.length() * 2);
        int i = 0;
        
        while (i < json.length()) {
            char c = json.charAt(i);
            
            // 处理非字符串部分的结构字符
            if (c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',') {
                result.append(c);
                i++;
                continue;
            }
            
            // 保留空白字符
            if (Character.isWhitespace(c)) {
                result.append(c);
                i++;
                continue;
            }
            
            // 处理字符串值
            if (c == '"') {
                result.append(c);
                i++;
                
                // 进入字符串内部，处理直到字符串结束
                i = processStringContent(json, i, result);
            } else {
                // 其他字符（null, true, false, 数字等）
                result.append(c);
                i++;
            }
        }
        
        return result.toString();
    }
    
    /**
     * 处理字符串内容
     * 
     * @param json 原始JSON字符串
     * @param startIndex 字符串内容开始位置
     * @param result 结果StringBuilder
     * @return 处理后的位置索引
     */
    private static int processStringContent(String json, int startIndex, StringBuilder result) {
        int i = startIndex;
        
        while (i < json.length()) {
            char ch = json.charAt(i);
            
            // 处理转义字符
            if (ch == '\\') {
                result.append(ch);
                i++;
                // 保留转义后的字符
                if (i < json.length()) {
                    result.append(json.charAt(i));
                    i++;
                }
                continue;
            }
            
            // 遇到引号，判断是结束还是内容
            if (ch == '"') {
                // 查找后续第一个非空白字符
                int nextCharIndex = findNextNonWhitespace(json, i + 1);
                
                if (nextCharIndex == -1) {
                    // 已到末尾，是字符串结束
                    result.append(ch);
                    i++;
                    break;
                }
                
                char nextChar = json.charAt(nextCharIndex);
                
                // 判断是否为结构字符
                if (isStructuralChar(nextChar)) {
                    // 是字符串结束
                    result.append(ch);
                    i++;
                    break;
                } else {
                    // 是内容中的引号，需要转义
                    result.append("\\\"");
                    i++;
                }
            } else {
                // 普通字符
                result.append(ch);
                i++;
            }
        }
        
        return i;
    }
    
    /**
     * 查找下一个非空白字符的位置
     */
    private static int findNextNonWhitespace(String str, int startIndex) {
        for (int i = startIndex; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 判断是否为JSON结构字符
     */
    private static boolean isStructuralChar(char c) {
        return c == ',' || c == ':' || c == '}' || c == ']';
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试用例1: 包含中文标点
        String test1 = "{\"name\"：\"张三\"，\"age\"：18}";
        System.out.println("测试1原始: " + test1);
        System.out.println("测试1修复: " + fix(test1));
        System.out.println();
        
        // 测试用例2: 包含未转义引号
        String test2 = "{\"dialogue\": \"李阎问:\"你在做什么？\"\"}";
        System.out.println("测试2原始: " + test2);
        System.out.println("测试2修复: " + fix(test2));
        System.out.println();
        
        // 测试用例3: 包含已转义引号（应保持不变）
        String test3 = "{\"dialogue\": \"李阎问:\\\"你在做什么？\\\"\"}";
        System.out.println("测试3原始: " + test3);
        System.out.println("测试3修复: " + fix(test3));
        System.out.println();
        
        // 测试用例4: 复杂情况
        String test4 = "[{\"standardName\":\"李阎\",\"relationships\":\"怪物称呼李阎为\"我的行走\",要求李阎\"唤醒她\"。\"}]";
        System.out.println("测试4原始: " + test4);
        System.out.println("测试4修复: " + fix(test4));
    }
}
