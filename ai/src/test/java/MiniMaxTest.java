import cn.hutool.core.collection.CollUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MiniMaxTest {
    static String prompt = """
                    	请基于SystemPrompt中定义的强制性约束规则，处理以下输入：
            
            【章节信息】
            - 章节标题：{{chapterTitle}}
            - 章节索引：{{chapterIndex}}
            
            【文本内容】
            lastContext（上一章完整内容）：
            ''
            
            作用：确认实体一致性、推断前置状态，**不提取新信息**
            
            ---
            
            indexText（当前章完整内容）：
                    第1章 大枪落幕
                    第一卷：城寨风云
                    河北，一座要在地图上找很久的小县城里。
                    她透过车窗打量着眼前布满灰尘和污渍的夜市，鼎沸的人声能传出好远，街上满是火锅店里传出来的，浓郁的罂粟壳的味道。
                    烧烤摊边的老板油光满面，来往的人裹紧了大衣穿过浓烟。
                    逼仄，凝涩，冷硬且粗粝。
                    北上广是这个国家的幻象，小县城才是这个国家的真相，女人以前听人说过类似的话。只是从来没有太深的体会。
                    这恐怕是自己最后的机会了，她叹了口气，说道：
                    “是这儿了，白叔，下车吧。”
                    北方这时候风大，女人裹着一件宽大风衣，把身体曲线都遮挡起来，她身后跟着一个三十多岁的男人，站姿笔直坚挺，留一个利落的平头。
                    两人一前一后踏进了一家破旧的音像店。
                    这个惨淡的行当在当下实在是不多见了。
                    老式的电视有些发潮，年轻的窦唯在发绿的屏幕里声嘶力竭。正赶上那句“你所拥有的是你的身体，动人的美丽，我所拥有的是我的记忆，美妙的感觉。”歌词污得不行。
                    一摞一摞的武侠小说堆得到处倒是，梁羽生，古龙。还有倪匡的都有，暖色玻璃柜里列着上世纪末的各色唱片和录像，还有一些像素模糊，乃至于黑白的老照片。泰迪罗宾，许冠杰，谭咏麟拿着话筒相望，旁边写着八四年太空之旅演唱会的字样。
                    墙上贴着老式海报，写着“胭脂扣”三个字，画上女扮男装的梅艳芳脖颈和眉眼都淡得像烟。
                    “来点什么？”
                    嗓音清朗温和，不像是个粗粝的北方男人。
                    让人跌破眼镜的是，这样老土的店，主人却是个高高瘦瘦的年轻人，模样看上去不超过二十五岁。他穿着一件黑色的T恤，面色苍白。
                    “请问你是李阎先生么？”
                    女人微笑着问。
                    “啊，我是。”
                    眼前这个穿着风衣的女人鼻梁高且挺拔，眼窝很浅，五官很漂亮，显得英气十足。整个人透出一股利落劲头。
                    李阎不着痕迹地瞥了一眼女人身后缄默的平头男人，回了一句。
                    “我是中华国术协会的理事，从广东来，我叫雷晶，雷洪生是我爷爷。”
                    女人露出一口洁白的牙齿：“论辈分，我应该叫你一声师兄才是。”
                    她握住李阎的巴掌，入手温润有力。
                    李阎的眼睛像是蒙了一层薄薄的灰尘，听到“雷洪生”这个名字才有一丝光彩透露出来。
                    “哦，坐，坐，地方小，别见怪。”
                    女人落落大方地坐在一旁的椅子上，默默打量着李阎，她家中还存放爷爷和这个男人早些时候的合照，却很难把照片里那个锐利桀骜的青年和眼前这个音像店的老板联系起来。
                    “我经常听爷爷提起师兄，他总念叨着，你是他见过的人里天分最高的。”
                    男人转身拿出暖壶，一边沏水一边问道。
                    “老爷子身体还硬朗？”
                    女人的眸子一低，“他老人家，年前去世了。”
                    李阎的手很稳，水半点也没洒出来，他放下暖壶，深深地看了女人一眼。
                    “有什么我能帮你的吗？”
                    女人抿了抿嘴唇，说道：“我希望李师兄能够跟我去广东，担任协会的顾问。”
                    李阎挑了挑眉毛，说道：
                    “我是个什么人，雷小姐应当所耳闻。说句有自知之明的话，在武术界，李阎两个字称得上声名狼藉。你怎么会认为，我能帮你。”
                    雷晶默然了一会儿，嫣然一笑道：
                    “与其说声名狼藉，倒不如说是凶名昭著，也许那些人不会尊敬师兄你，但是他们一定会怕你。”
                    李阎闻言不禁笑出了声。
                    “听上去很有道理。可惜你来晚了。”
                    男人端起杯子：“雷小姐对现代医学有了解么？”
                    “额，师兄您指？”
                    “aml。”
                    雷晶愣了一下，接着心头涌上一股阴霾。她试探着开口说道：“急性髓细胞白血病。”
                    李阎抿了一口水：“一个月之前我被确诊患上了这种病，你家老爷子清楚，我这个人无亲无故。”
                    他笑了笑：“所以，我恐怕帮不了你了。”
                    女人低头看了一会指甲，才干涩地说：“南方的医疗条件比这里要好很多，我也认识一些国外的知名医生，白血病算不上绝症，即使师兄你没有兄弟姐妹，也完全有可能找到配对的骨髓。”
                    女人抬起头来，整个人凌厉了许多。
                    “可能我这次来让师兄很为难，但是……”
                    雷晶斟酌着字眼：“协会是我爷爷一生的心血，我不能眼睁睁地看着它沦为一些政客弄权或牟利的工具。”
                    “你说弄权……”
                    李阎忽然打断了女人的话，他把热水饮尽，把玩着手里的杯子。冲着女人一笑，身上莫名多了几分嚣烈的味道。
                    “为什么把协会交到你手里，就不是弄权？你能不能告诉我，你跟你厌恶的那些政客，区别在哪呢？”
                    李阎的话说得十分不客气，惹得一旁的平头男人皱紧了眉头。
                    女人的脸色很平静，只是慢条斯理地解释：“国术协会是我爷爷一手创立，我从十六岁开始接触协会的相关事宜，没有人比我更了解它，也没有人比我更热爱它。”
                    李阎摇了摇头，他放下杯子：“刚才我跟雷小姐握手，你的手很嫩，没练过武吧。”
                    女人抿紧了嘴唇。
                    “我从小身体不好，家里的功夫又霸道。所以只练了一些调养气息的吐纳功夫。”
                    “所以啊。”李阎低着头，忽然扯了句题外话：
                    “雷小姐喜欢看武侠小说么？”
                    雷晶被问得有些发蒙，她尝试着回答说：“金庸？”
                    “老舍，断魂枪。”
                    女人显然没听懂李阎什么意思，倒是一旁的平头男人眯了眯眼睛。
                    “总之，我这病秧子，真的没什么余力能帮你的忙，谢谢你的好意，如果你们两个想留下吃顿饭的话，我煮了饺子，如果不想，请便吧。还有，替我向老爷子上炷香。”
                    话说到这个份上，显然没有谈下去的必要了。
                    好一会儿，雷晶才默默地站了起来，却没有立刻离开，而是在李阎的注视下掏出一张名片放在了桌子上。
                    她说道：“有太多人跟我说起过，李阎是个多么跋扈的人，他们一定想不到师兄你现在的模样。”
                    李阎歪了歪脑袋，没有说话。
                    “可我爷爷一直很欣赏你，一直都是，你知道他是怎么评价你的么？”
                    雷晶直视着男人，学着自己爷爷的口气。
                    “习武之人，心头先养三分恶气，我这辈子见过这么多后生，只有这混小子不多不少，养足这三分恶气。”
                    “无论师兄你答不答应我的请求，我都真心希望师兄心头这三分恶气，没散。”
                    说完，雷晶转过身，和中年男人离开了。
                    李阎呆呆地坐了一会儿，弯腰把两杯热水端起来喝完。才噗嗤一笑；
                    “真是个厉害的丫头。”
                    他拿起纸巾抹了抹鼻子，也不在意纸上的一片殷红，随手丢到旁边。整个人躺倒在沙发上。
                    “三分恶气……嘿嘿。”
                    李阎用右手遮住自己的额头，回想起那位精神矍铄的浓眉老人，笑容中多了几分苦涩。
                    “对不住了，老爷子……”
                    “哒～”
                    一双锃亮的黑色皮鞋踩在了自家的地板上，李阎认得出皮鞋的主人，正是那名跟在雷晶身后的平头男人。
                    “还有什么事么？”
                    李阎坐起身来，抬头一看，顿时背脊一凉，栗色的瞳孔不住收缩。
                    门框被男人苍白的手指捏得咯咯作响，他野兽一般埋着身子，脸上鲜红的皮肉一点点向下垂落，粘连着丝状的发白的筋膜。整张脸已经糜烂不堪。
                    听到李阎的问话，这个男人缓缓抬头，沾满血丝的眼球向外突着，狰狞如同厉鬼。
                    李阎把冰冷的空气一点点吸进肺叶，伴随着电视屏幕里激昂的打击乐，是他短促有力的骂声。
                    “操！”
            
            作用：**唯一的信息提取来源**，所有Cypher必须基于此生成
            
            ---
            
            nextContext（下一章完整内容）：
            第2章 貘与姑获鸟
            这绝对不是人！
            在李阎转念的同时，那狰狞可怖的怪物竟是一记凌厉的鞭腿轰了过来。如同一道黑色的闪电击向李阎的太阳穴。
            李阎下意识伸出胳膊去档，却被庞大的力道轰得侧飞出去。
            没等李阎站起来，一条霸道的黑影当头砸下，他躲避不及，被一脚砸中右肩，整条胳膊酸麻难当，紧接着眼前一花，阴毒的鞋尖朝他面门而来。
            这连珠炮似的出腿骤雨一般降临，他根本避无可避，一旦这一脚砸实，李阎非死即残。
            他想也不想，双手迎向平头男人飞起的腿。左手手指悄无声息对着男人膝盖侧翼狠狠一剜！
            “啪！”
            “扑通。”
            平头男人的右脚踹中了李阎的手肘，可他整个人却扑通跪在了地上。
            那张皮肉模糊的脸正对着李阎。
            电光石火之间，一直被动挨打的李阎伸出手抓住平头男的肩膀，身子向后一仰，平头男上半身不受控制地跟着向前倾倒！
            李阎眼中有戾气闪过，紧接着就地翻身，右腿压住男人脖颈，只听见咔吧一声，平头男人整条胳膊被硬生生扯断！
            紧接着抵住平头男人后腰，抓起他另一只胳膊，干净利落地一拉一扯，不顾令人齿酸的骨骼断裂声音，右脚重重地踹向男人的胫骨，这一脚又凶又准，平头男的小腿被踢到错位，不规则地往外扭曲。
            他这才站起了身，居高临下地俯视着趴在地上不断挣扎着的平头男人，目光尤其在那张皮肉腐烂的脸上逗留了许久。
            看着半天爬不起来的平头男人，李阎长出一口气，转过头想去拿自己的手机。而趴在地上的男人居然这个时候，完全不可思议的暴起！张开惨白的牙齿咬向李阎的大腿！
            仿佛背后长了眼睛，李阎拧腰旋身飞踢，右脚带着风声，狠辣地踢在男人的太阳穴上。
            平头男的颈骨被这一脚硬生生踢断！脑袋如同一颗断裂的发条，拧了足足九十度，以一个诡异的角度挂在了脖子上。惨烈得难以表述。
            李阎舔了舔牙龈，吐出一口血水：
            “还他妈挺唬人。”
            他凶戾的目光来回扫视着男人的尸体，带着血腥味的冰冷气焰肆意喷薄，哪还有半点音像店主的温吞在？
            “精彩，真是精彩，谭腿的单展贯耳讲求气势连绵。一旦抢得先机，对手基本不可能翻盘，没想到却被一手凶奇的白马翻蹄反败为胜，都说河间李家枪剑双绝，手上的擒拿功夫也犀利得很啊。”
            他抬起头，看着站立在门口的一个身材五短的胖子。
            那人穿着一件白色背心，喇叭裤，拖鞋。头发发油，很邋遢。用死肥宅三个字来概括简直是严丝合缝，没有半点不合适的地方。
            而李阎却敏锐的发现，胖子身后的一片漆黑，门外没有月光，没有霓虹灯，没有烧烤的烟气和汽车的轰鸣，只有一片黏稠的黑，黑得可怖。
            “那女人呢？”
            李阎逼视着眼前的胖子。
            “接受能力很强，身手和反应都是一流，很好。”
            感觉到李阎的目光越发不善，胖子的脸上带着笑指了指地面。
            李阎用余光一扫，却发现地上的男人尸体竟然消失不见了！
            只有躺倒的沙发和一片狼藉能证明，刚刚的一切确实发生了。
            “我做梦了么？”
            李阎抚摸着额头，也许什么都没有发生，自己喝了三杯热水就倒在沙发上睡着了，没有什么皮鞋，脸上掉肉的男人，什么都没有。
            “我跟刚才那两个人没有任何关系，唯一的相同点在于，我们都对你很感兴趣，别担心，我只是借用那个平头男人的一个念头来向你打个招呼，仅此而已，他们两个对此一无所知。”
            李阎没说话，等着胖子的后文。
            “嗯，做个自我介绍吧，哎，我叫什么来着？”
            胖子把手伸进口袋里，翻出一大顿东西，诸如歪七扭八的维生素包装纸，画着裸女的理发店折扣卡，用过的不明卫生纸团等等。最终，终于翻出了一张带着污垢的身份证。
            上面清楚地写着王x阳三个大字。
            “总之，你叫我王阳就好了，不过，你也可以叫我另外一个名字。”
            胖子诡异地一笑，
            “貘……”
            李阎看了他一眼。呵了一声。
            “魔，吃饺子么？”
            王阳有一瞬间的错愕，随即就笑出了声，而且笑得愈发癫狂。
            “你，哈哈，你太有趣了。哈哈哈……”
            李阎眯了眯眼，他可以对三祖太爷发誓，自己很想把脚下的皮鞋恶狠狠地印在这个油腻胖子的脸上，前提是自己能做到。
            “我可以救你的命。”胖子忽然不笑了。
            李阎盯着他问：“你有我的配对骨髓？”
            “那太小儿科了。”王阳摇着头：“我可以让你拥有更好的。”
            李阎把双手垂下。沉吟了好一会儿，直到王阳有些不耐烦了。
            “我能为你做什么？”
            王阳摊了摊手。
            “试试看不就知道了，不过，这是条不能回头的路……”
            “听上去比等死强多了。”
            李阎眼里有冷厉的光一闪而逝。
            “来吧，说出来听听。”
            胖子咧嘴一笑，脸色变得异常阴暗而肃穆：“那么，来吧。”
            他身后，一片深渊般的黑色浪潮汹涌而出，将惊骇的李阎整个吞没。
            ※※※
            那一刻，李阎感觉自己在下坠，黑色将自己笼罩，眼前浮现出无数光怪陆离，不可思议的景象。
            横贯天际的黑色锁链，数千万斤绿铜浇注的巍峨大殿，旋舞在天际的金红色信天翁，黑沉沉的乌云中，孕育着蓝紫色的浓郁雷浆，以及大殿当中，注视着自己的，无数道庞大神秘的阴影。
            无比滚烫的血液烧得李阎眼前一片通红。而猩红色眼帘之中，他看清那些庞大神秘黑影之中的一个……
            十八道翅膀长短相接，九颗头颅显得凶恶而怪异，一颗脖颈鲜血淋漓……
            “唤醒她，我的行走。”
            “你是谁？”
            李阎发不出声音，只能在心头呐喊。
            冰冷，黑暗。
            
            作用：消除歧义、理解语境，**不生成Cypher**
            
            【关键约束】
            - Event.chapterIndex 必须使用：{{chapterIndex}}
            - Event.source 格式：第{{chapterIndex}}章 {{chapterTitle}}
            - Event节点不包含paragraphIndex属性
            - State.valid_from_chapter 必须等于Event.chapterIndex
            - State.valid_to_chapter 在状态转换时必须设置为新Event.chapterIndex
            
            请严格遵循SystemPrompt的RULE-1至RULE-6 (kgKnowlage.md)，生成符合规范的Cypher语句。
            
            **输出规范：**
            1. ⚠️ **禁止Markdown代码块！** 不允许使用```cypher```或```包裹，直接输出Cypher语句
            2. 禁止输出任何自然语言解释
            3. 如indexText无新信息，必须返回空字符串
            4. 使用MERGE保证幂等性，避免重复创建
            5. 节点标签使用双标签：:Entity:Character, :Event:StoryEvent
            6. 关系类型只能有一个名称，不能使用多标签（如[:RELATION:father]）
            7. **Cypher变量引用规则：**
               - ❌ 错误: `MERGE (c1:Character {...}) ... MERGE (e)-[:MENTIONS]->(c1 {name: "xxx"})` 
               - ✅ 正确: `MERGE (c1:Character {...}) ... MERGE (e)-[:MENTIONS]->(c1)`
               - **说明:** 已声明的变量不能再次添加属性或标签，直接引用变量名即可
            8. 跨章节实体延伸约束
               - 第一章实体在后续章节的信息延伸必须标注来源章节
               - 通过Event.description属性记录跨章节信息来源
            
            **错误示例（绝对禁止）：**
            ```cypher
            MERGE (c:Entity:Character {...})
            ```
            
            **正确示例：**
            MERGE (c:Entity:Character {...})
            MERGE (e:Event:StoryEvent {...})
            MERGE (c)-[:PARTICIPATED_IN]->(e)
            
            
            
            
            
            """;

    public static void main(String[] args) throws Exception {
		OpenAiChatModel chatModel = OpenAiChatModel.builder()
				.baseUrl("https://api.minimaxi.com/v1")
                .apiKey("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJHcm91cE5hbWUiOiJTaHVhbmdsaW4iLCJVc2VyTmFtZSI6IlNodWFuZ2xpbiIsIkFjY291bnQiOiIiLCJTdWJqZWN0SUQiOiIxOTg1NjUzMDM4MDkzNzA1NTkwIiwiUGhvbmUiOiIxODc3Nzc5MTY0NSIsIkdyb3VwSUQiOiIxOTg1NjUzMDM4MDg1MzE2OTgyIiwiUGFnZU5hbWUiOiIiLCJNYWlsIjoiIiwiQ3JlYXRlVGltZSI6IjIwMjUtMTEtMDYgMTQ6MzQ6NDUiLCJUb2tlblR5cGUiOjEsImlzcyI6Im1pbmltYXgifQ.CIsWfl6R1lfBH34ya0Q1H0zYFHT4bQ5LhJAnH4Q6JGgnPXZ-Xp_CVITmk7Nspbck5EkOGuaKe5zrqfaXyfK_3MuItTwY8Qj3YTrGJanX1dIZGLELBNdOExClVDTZLPNK5c5YOilvGczo5Uw7EMnJIb_WGBgFbYKBOyL1M4pGLnrcOtwlDZ-kIZ2Ifgee9JqVY5Y4sVpvsJA3G2JiP9Cb5q24GXrWEvZlcxg-QAqOKwbiPuki_hI6dI_6pdKrUQwm6Iu8iC-xZP6Akayn4GZ6XDBCcne4gMkYVMARAIWyhIfZbeLkS7tyMItadqAgE6aCG6fRRa6xXgZ2RXDUEr4Phg")
				.modelName("MiniMax-M2")
                .customHeaders(Map.of("reasoning_split","true"))
				.temperature(0.0)
				.build();
        ClassPathResource resource = new ClassPathResource("prompt/SystemPrompt-3.0.md");
        InputStream inputStream = resource.getInputStream();
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        List<ChatMessage> messages = CollUtil.newArrayList(
                SystemMessage.systemMessage(content.toString()),
                UserMessage.userMessage(prompt)
        );
        ChatResponse chatResponse = chatModel.chat(messages);
        String s = removeThinkingTags(chatResponse.aiMessage().text());
        System.out.println(" = ");
        System.out.println(s);

//        GoogleAiGeminiChatModel geminiChatModel = GoogleAiGeminiChatModel.builder()
//                .apiKey("AIzaSyDf8AumGRKxpZwWGVTYsr3hlxeXZPQ9quQ")
//                .modelName("gemini-2.5-pro")
//                .temperature(0.7)
//                .build();
//        System.out.println("geminiChatModel.chat(messages) = " + geminiChatModel.chat(messages));
    }

    public static String removeThinkingTags(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 去除XML格式的thinking标签及其内容
        String result = input.replaceAll("<thinking>.*?</thinking>", "");

        // 去除简化格式的thinking标签及其内容
        result = result.replaceAll("(?s)<think>.*?</think>", "");

        // 清理多余的空白字符
        result = result.replaceAll("\\s+", " ").trim();

        return result;
    }
}