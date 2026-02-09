MERGE (c:Chapter {chapterIndex: 1})
  ON CREATE SET c.name = '第1章 大枪落幕'
MERGE (e1:Entity:Character {uuid: randomUUID(), name: '李阎', entityType: 'Character', createdAtChapter: 1})
MERGE
  (s1:State {uuid:        randomUUID(), chapterIndex: 1, stateType: '身份', stateValue: '音像店店主',
             description: '河北小县城破旧音像店的老板'})
MERGE (e1)-[:HAS_STATE]->(s1)
MERGE (c)-[:HAS_CONTENT]->(e1)
MERGE (c)-[:HAS_CONTENT]->(s1)
MERGE (e2:Entity:Character {uuid: randomUUID(), name: '雷晶', entityType: 'Character', createdAtChapter: 1})
MERGE
  (s2:State {uuid:        randomUUID(), chapterIndex: 1, stateType: '身份', stateValue: '中华国术协会理事',
             description: '从广东来的年轻女子，雷洪生的孙女'})
MERGE (e2)-[:HAS_STATE]->(s2)
MERGE (c)-[:HAS_CONTENT]->(e2)
MERGE (c)-[:HAS_CONTENT]->(s2)
MERGE (e3:Entity:Character {uuid: randomUUID(), name: '雷洪生', entityType: 'Character', createdAtChapter: 1})
MERGE
  (s3:State {uuid:        randomUUID(), chapterIndex: 1, stateType: '生存状态', stateValue: '已故',
             description: '雷晶的爷爷，国术协会创始人'})
MERGE (e3)-[:HAS_STATE]->(s3)
MERGE (c)-[:HAS_CONTENT]->(e3)
MERGE (c)-[:HAS_CONTENT]->(s3)
MERGE (e4:Entity:Character {uuid: randomUUID(), name: '平头男人', entityType: 'Character', createdAtChapter: 1})
MERGE
  (s4:State {uuid:        randomUUID(), chapterIndex: 1, stateType: '身份', stateValue: '雷晶随从',
             description: '跟随雷晶的三十多岁男人，站姿笔直'})
MERGE (e4)-[:HAS_STATE]->(s4)
MERGE (c)-[:HAS_CONTENT]->(e4)
MERGE (c)-[:HAS_CONTENT]->(s4)
MERGE
  (ev1:Event:StoryEvent {uuid:        randomUUID(), chapterIndex: 1, eventType: '拜访',
                         description: '雷晶带着平头男人到音像店拜访李阎，邀请其担任协会顾问',
                         sourceText:  '雷晶微笑着问：请问你是李阎先生么？我叫雷晶，雷洪生是我爷爷。',
                         source:      '第1章 大枪落幕', isImplicit: false})
MERGE (c)-[:HAS_CONTENT]->(ev1)
MERGE (e2)-[:PARTICIPATES_IN {role: '拜访者'}]->(ev1)
MERGE (e4)-[:PARTICIPATES_IN {role: '随从'}]->(ev1)
MERGE (e1)-[:PARTICIPATES_IN {role: '被拜访者'}]->(ev1)
MERGE
  (ev2:Event:StoryEvent {uuid:        randomUUID(), chapterIndex: 1, eventType: '对话',
                         description: '李阎向雷晶透露自己患急性髓细胞白血病，无亲无故，无法帮忙',
                         sourceText:  '一个月之前我被确诊患上了这种病，你家老爷子清楚，我这个人无亲无故。',
                         source:      '第1章 大枪落幕', isImplicit: false})
MERGE (c)-[:HAS_CONTENT]->(ev2)
MERGE (e1)-[:PARTICIPATES_IN {role: '疾病披露者'}]->(ev2)
MERGE (e2)-[:PARTICIPATES_IN {role: '倾听者'}]->(ev2)
MERGE
  (ev3:Event:StoryEvent {uuid:        randomUUID(), chapterIndex: 1, eventType: '质疑',
                         description: '李阎质疑雷晶与政客的区别，指出她的手很嫩没练过武',
                         sourceText:  '刚才我跟雷小姐握手，你的手很嫩，没练过武吧。', source: '第1章 大枪落幕',
                         isImplicit:  false})
MERGE (c)-[:HAS_CONTENT]->(ev3)
MERGE (e1)-[:PARTICIPATES_IN {role: '质疑者'}]->(ev3)
MERGE (e2)-[:PARTICIPATES_IN {role: '被质疑者'}]->(ev3)
MERGE
  (ev4:Event:StoryEvent {uuid:        randomUUID(), chapterIndex: 1, eventType: '离去与名片',
                         description: '雷晶留下名片后离开，说出爷爷对李阎的评价',
                         sourceText:  '有太多人跟我说起过，李阎是个多么跋扈的人...习武之人，心头先养三分恶气',
                         source:      '第1章 大枪落幕', isImplicit: false})
MERGE (c)-[:HAS_CONTENT]->(ev4)
MERGE (e2)-[:PARTICIPATES_IN {role: '离别者'}]->(ev4)
MERGE (e3)-[:PARTICIPATES_IN {role: '评价者'}]->(ev4)
MERGE (e1)-[:PARTICIPATES_IN {role: '被评价者'}]->(ev4)
MERGE
  (ev5:Event:StoryEvent {uuid:        randomUUID(), chapterIndex: 1, eventType: '怪变',
                         description: '平头男人突然变成恐怖的怪物袭击李阎',
                         sourceText:  '门框被男人苍白的手指捏得咯咯作响，他野兽一般埋着身子，脸上鲜红的皮肉一点点向下垂落',
                         source:      '第1章 大枪落幕', isImplicit: false})
MERGE (c)-[:HAS_CONTENT]->(ev5)
MERGE (e4)-[:PARTICIPATES_IN {role: '怪物攻击者'}]->(ev5)
MERGE (e1)-[:PARTICIPATES_IN {role: '受害者'}]->(ev5)
MERGE (e5:Entity:Location {uuid: randomUUID(), name: '河北小县城', entityType: 'Location', createdAtChapter: 1})
MERGE
  (s5:State {uuid:        randomUUID(), chapterIndex: 1, stateType: '地理位置', stateValue: '李阎居住地',
             description: '河北的一座小县城，李阎音像店所在地'})
MERGE (e5)-[:HAS_STATE]->(s5)
MERGE (c)-[:HAS_CONTENT]->(e5)
MERGE (c)-[:HAS_CONTENT]->(s5)
MERGE
  (s6:State {uuid:        randomUUID(), chapterIndex: 1, stateType: '健康状况', stateValue: '急性髓细胞白血病',
             description: '李阎一个月前被确诊患有的疾病'})
MERGE (e1)-[:HAS_STATE]->(s6)
MERGE (c)-[:HAS_CONTENT]->(s6)