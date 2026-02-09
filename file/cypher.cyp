MERGE (c:Chapter {chapterIndex: 1})
  ON CREATE SET c.name = '第1章 大枪落幕'
  ON CREATE SET c.summary = '李阎经营音像店，雷晶寻找其担任国术协会顾问，李阎因患AML白血病拒绝。后平头男人异变成怪物攻击李阎，被李阎击杀。最后出现神秘胖子王阳。'
MERGE
  (e1:Entity:Character {uuid: randomUUID(), name: '雷晶', entityType: 'Character', createdAtChapter: 1, alias: [
    '雷小姐', '师兄']})
SET e1.gender = 'female', e1.ageApproximate = 26, e1.background = '雷洪生孙女，中华国术协会理事', e1.
  appearance = '鼻梁高挺，眼窝浅，五官漂亮，英气十足', e1.clothing = '宽大风衣'
MERGE
  (e2:Entity:Character {uuid: randomUUID(), name: '李阎', entityType: 'Character', createdAtChapter: 1, alias: [
    '李师兄']})
SET e2.gender = 'male', e2.ageApproximate = 25, e2.appearance = '高高瘦瘦，面色苍白', e2.occupation = '音像店老板', e2.
  clothing = '黑色T恤', e2.personality = '早年锐利桀骜，现在温吞内敛'
MERGE (e3:Entity:Character {uuid: randomUUID(), name: '平头男人', entityType: 'Character', createdAtChapter: 1})
SET e3.gender = 'male', e3.ageApproximate = 35, e2.appearance = '留利落平头，站姿笔直坚挺', e3.role = '雷晶随从'
MERGE (e4:Entity:Character {uuid: randomUUID(), name: '雷洪生', entityType: 'Character', createdAtChapter: 1})
SET e4.gender = 'male', e4.status = '已故', e4.background = '中华国术协会创始人', e4.
  personality = '浓眉老人，精神矍铄', e4.relationship = '雷晶爷爷'
MERGE
  (e5:Entity:Organization {uuid: randomUUID(), name: '中华国术协会', entityType: 'Organization', createdAtChapter: 1})
SET e5.orgType = '武术协会', e5.founder = '雷洪生', e5.currentLeader = '雷晶', e5.status = '面临沦为政客工具的危险'
MERGE (e6:Entity:Location {uuid: randomUUID(), name: '河北小县城音像店', entityType: 'Location', createdAtChapter: 1})
SET e6.locationType = '店铺', e6.description = '破旧音像店，播放窦唯歌曲，堆满武侠小说和磁带', e6.atmosphere = '惨淡、老土'
MERGE
  (ev1:Event:StoryEvent {uuid:        randomUUID(), chapterIndex: 1, eventType: '对话',
                         description: '雷晶拜访李阎，邀请其担任中华国术协会顾问',
                         sourceText:  '我是中华国术协会的理事，从广东来，我叫雷晶，雷洪生是我爷爷。论辈分，我应该叫你一声师兄才是。我希望李师兄能够跟我去广东，担任协会的顾问。',
                         source:      '第1章 大枪落幕'})
MERGE
  (ev2:Event:StoryEvent {uuid:        randomUUID(), chapterIndex: 1, eventType: '对话',
                         description: '李阎告知雷晶自己患有AML白血病，拒绝邀请',
                         sourceText:  '一个月之前我被确诊患上了这种病，你家老爷子清楚，我这个人无亲无故。所以，我恐怕帮不了你了。',
                         source:      '第1章 大枪落幕'})
MERGE
  (ev3:Event:StoryEvent {uuid:        randomUUID(), chapterIndex: 1, eventType: '异变攻击',
                         description: '平头男人异变成怪物攻击李阎',
                         sourceText:  '一双锃亮的黑色皮鞋踩在了自家的地板上，李阎认得出皮鞋的主人，正是那名跟在雷晶身后的平头男人。门框被男人苍白的手指捏得咯咯作响，他野兽一般埋着身子，脸上鲜红的皮肉一点点向下垂落，粘连着丝状的发白的筋膜。整张脸已经糜烂不堪。',
                         source:      '第1章 大枪落幕'})
MERGE
  (ev4:Event:StoryEvent {uuid:        randomUUID(), chapterIndex: 1, eventType: '战斗',
                         description: '李阎击杀异变的平头男人',
                         sourceText:  '李阎眼中有戾气闪过，紧接着就地翻身，右腿压住男人脖颈，只听见咔吧一声，平头男人整条胳膊被硬生生扯断！紧接着抵住平头男人后腰抓起他另一只胳膊，干净利落地一拉一扯...平头男的颈骨被这一脚硬生生踢断！',
                         source:      '第1章 大枪落幕'})
MERGE (e1)-[:PARTICIPATES_IN {role: '拜访者'}]->(ev1)
MERGE (e2)-[:PARTICIPATES_IN {role: '被拜访者'}]->(ev1)
MERGE (c)-[:HAS_CONTENT]->(ev1)
MERGE (e2)-[:PARTICIPATES_IN {role: '告知者'}]->(ev2)
MERGE (e1)-[:PARTICIPATES_IN {role: '倾听者'}]->(ev2)
MERGE (c)-[:HAS_CONTENT]->(ev2)
MERGE (e3)-[:PARTICIPATES_IN {role: '攻击者'}]->(ev3)
MERGE (e2)-[:PARTICIPATES_IN {role: '被攻击者'}]->(ev3)
MERGE (c)-[:HAS_CONTENT]->(ev3)
MERGE (e2)-[:PARTICIPATES_IN {role: '战斗者'}]->(ev4)
MERGE (e3)-[:PARTICIPATES_IN {role: '被击杀者'}]->(ev4)
MERGE (c)-[:HAS_CONTENT]->(ev4)
MERGE
  (s1:State {uuid:        randomUUID(), chapterIndex: 1, stateType: '健康状况', stateValue: 'AML白血病确诊',
             description: '李阎在一个月前被确诊急性髓细胞白血病'})
MERGE (e2)-[:HAS_STATE]->(s1)
MERGE (c)-[:HAS_CONTENT]->(s1)
MERGE
  (s2:State {uuid:        randomUUID(), chapterIndex: 1, stateType: '身份', stateValue: '国术协会理事',
             description: '雷晶作为雷洪生孙女，担任中华国术协会理事'})
MERGE (e1)-[:HAS_STATE]->(s2)
MERGE (c)-[:HAS_CONTENT]->(s2)
MERGE
  (s3:State {uuid:        randomUUID(), chapterIndex: 1, stateType: '居住状态', stateValue: '经营音像店',
             description: '李阎经营破旧音像店，生活状况一般'})
MERGE (e2)-[:HAS_STATE]->(s3)
MERGE (c)-[:HAS_CONTENT]->(s3)
MERGE (ev1)-[:OCCURRED_IN]->(e6)
MERGE (ev2)-[:OCCURRED_IN]->(e6)
MERGE (ev3)-[:OCCURRED_IN]->(e6)
MERGE (ev4)-[:OCCURRED_IN]->(e6)
MERGE (c)-[:HAS_CONTENT]->(e1)
MERGE (c)-[:HAS_CONTENT]->(e2)
MERGE (c)-[:HAS_CONTENT]->(e3)
MERGE (c)-[:HAS_CONTENT]->(e4)
MERGE (c)-[:HAS_CONTENT]->(e5)
MERGE (c)-[:HAS_CONTENT]->(e6)