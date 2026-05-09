<template>
  <div class="reading-page">
    <!-- 顶部状态栏 -->
    <div class="reading-header">
      <button class="back-btn" @click="confirmExit">
        <span>←</span> 退出阅读
      </button>
      <div class="reading-info">
        <span class="book-title">{{ book?.title }}</span>
        <span class="child-name">👶 {{ child?.name }}正在阅读</span>
      </div>
      <div class="header-stats">
        <div class="stat-mini">
          <span>⏱️</span>
          <span>{{ formattedDuration }}</span>
        </div>
        <div class="stat-mini">
          <span>📖 翻页</span>
          <span>{{ pageTurnCount }}次</span>
        </div>
        <div class="stat-mini">
          <span>📊 进度</span>
          <span>{{ progressPercent }}%</span>
        </div>
      </div>
    </div>

    <!-- 绘本阅读区域 -->
    <div class="reading-content">
      <div class="book-display">
        <div class="page-container">
          <div class="current-page" :style="getPageStyle">
            <div class="page-content">
              <div class="illustration-area">
                <span class="illustration-emoji">{{ currentPageData.illustration }}</span>
              </div>
              <div class="story-area">
                <p class="story-text">{{ currentPageData.text }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="page-controls">
          <button class="page-btn prev" :disabled="currentPage <= 1" @click="prevPage">
            <span>◀</span> 上一页
          </button>
          <span class="page-indicator-inline">第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button class="page-btn next" :disabled="currentPage >= totalPages" @click="nextPage">
            下一页 <span>▶</span>
          </button>
        </div>
      </div>

      <!-- 底部操作区 -->
      <div class="bottom-bar">
        <div class="interaction-btns">
          <button class="interact-btn" :class="{ active: isBookmarked }" @click="toggleBookmark">
            <span>{{ isBookmarked ? '❤️' : '🤍' }}</span>
            <span class="btn-text">收藏</span>
          </button>
          <button class="interact-btn" @click="openAnnotationDialog">
            <span>📝</span>
            <span class="btn-text">批注</span>
          </button>
          <button class="interact-btn" :class="{ recording: isRecording }" @click="toggleRecording">
            <span>{{ isRecording ? '⏹️' : '🎤' }}</span>
            <span class="btn-text">{{ isRecording ? `${recordingTime}s` : '录音' }}</span>
          </button>
          <button class="interact-btn" @click="showAnnotationList = true">
            <span>📋</span>
            <span class="btn-text">记录</span>
          </button>
        </div>
        <div class="reading-tips-inline">
          <span>💡</span>
          <span>{{ currentTip }}</span>
        </div>
        <button class="finish-btn" @click="finishReading">
          ✅ 完成阅读
        </button>
      </div>
    </div>

    <div class="confirm-modal" v-if="showExitConfirm">
      <div class="confirm-content">
        <h3>确定要退出阅读吗？</h3>
        <p>当前阅读进度将被保存</p>
        <div class="confirm-actions">
          <button class="cancel-btn" @click="showExitConfirm = false">继续阅读</button>
          <button class="confirm-btn" @click="exitReading">确认退出</button>
        </div>
      </div>
    </div>

    <!-- 批注弹窗 -->
    <div class="confirm-modal" v-if="showAnnotationDialog" @click.self="showAnnotationDialog = false">
      <div class="annotation-dialog">
        <div class="dialog-header">
          <h3>📝 添加批注</h3>
          <button class="close-btn" @click="showAnnotationDialog = false">✕</button>
        </div>
        <div class="dialog-body">
          <p class="page-info">第 {{ currentPage }} 页</p>
          <textarea v-model="annotationText" placeholder="写下你的想法..." rows="4"></textarea>
        </div>
        <div class="dialog-footer">
          <button class="cancel-btn" @click="showAnnotationDialog = false">取消</button>
          <button class="confirm-btn" @click="submitAnnotation">保存</button>
        </div>
      </div>
    </div>

    <!-- 记录列表弹窗 -->
    <div class="confirm-modal" v-if="showAnnotationList" @click.self="showAnnotationList = false">
      <div class="annotation-list-dialog">
        <div class="dialog-header">
          <h3>📋 互动记录</h3>
          <button class="close-btn" @click="showAnnotationList = false">✕</button>
        </div>
        <div class="dialog-body">
          <div class="stats-summary">
            <div class="stat-item">
              <span class="stat-icon">❤️</span>
              <span class="stat-value">{{ bookmarkCount }}</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">📝</span>
              <span class="stat-value">{{ annotationCount }}</span>
              <span class="stat-label">批注</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">🎤</span>
              <span class="stat-value">{{ voiceRecordCount }}</span>
              <span class="stat-label">录音</span>
            </div>
          </div>
          <div class="annotation-list" v-if="annotations.length > 0">
            <div class="annotation-item" v-for="item in annotations" :key="item.id">
              <div class="item-header">
                <span class="item-page">第{{ item.pageNum }}页</span>
                <span class="item-type">{{ item.annotationType === 1 ? '📝 文字' : '🎤 语音' }}</span>
                <button class="delete-btn" @click="handleDeleteAnnotation(item.id)">🗑️</button>
              </div>
              <!-- 文字批注 -->
              <p v-if="item.annotationType === 1" class="item-content">{{ item.content }}</p>
              <!-- 语音批注 -->
              <div v-else class="audio-player">
                <button class="play-btn" @click="playAudio(item.content)">▶️ 播放录音</button>
              </div>
            </div>
          </div>
          <div class="empty-list" v-else>
            <span>📭</span>
            <p>暂无批注记录</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBookById } from '@/api/book'
import { getChildById } from '@/api/child'
import { createReadingLog } from '@/api/readingLog'
import { addBookmark, removeBookmark, checkBookmark } from '@/api/bookmark'
import { addAnnotation, getBookAnnotations, deleteAnnotation } from '@/api/annotation'
// 所有绘本故事内容
const storyLibrary = {
  // ===== 动物类 =====
  '小熊的冒险': [
    { illustration: '🐻🌳', text: '在一片茂密的森林里，住着一只可爱的小熊。它的毛是棕色的，眼睛像两颗亮晶晶的黑葡萄。' },
    { illustration: '🐻🍯', text: '小熊最喜欢吃蜂蜜了！每天早上，它都会去森林里找蜜蜂朋友，用自己采来的野花换取香甜的蜂蜜。' },
    { illustration: '🐰🌸', text: '一天，小熊遇到了一只迷路的小兔子。"小熊哥哥，我找不到回家的路了..."小兔子着急得快要哭出来了。' },
    { illustration: '🐻🗺️', text: '"别担心，小兔子！我带你回家！"小熊牵起小兔子的手，穿过森林，跨过小溪，终于找到了小兔子的家。' },
    { illustration: '🐰❤️', text: '小兔子的妈妈感激地说："谢谢你，小熊！你真是一个善良的好孩子！"从那以后，小熊和小兔子成了最好的朋友。' },
    { illustration: '🌟💕', text: '小熊明白了，帮助别人是一件很快乐的事情。从那以后，森林里的小动物们都喜欢和善良的小熊做朋友。' }
  ],
  '森林音乐会': [
    { illustration: '🌲🎵', text: '森林里要举办一场盛大的音乐会！小动物们都在忙着准备自己的节目。' },
    { illustration: '🐦🎤', text: '小鸟正在练习唱歌，它的歌声清脆悦耳，像银铃一样好听。"啦啦啦，森林多美好！"' },
    { illustration: '🐰🥁', text: '小兔子拿来了一面大鼓，咚咚咚地敲着节奏。它说："我来为大家伴奏！"' },
    { illustration: '🦊🎻', text: '小狐狸带来了一把小提琴，它拉出的曲子优美动听，让大家陶醉其中。' },
    { illustration: '🐻🎺', text: '小熊吹起了小号，虽然它吹得有点跑调，但大家都为它鼓掌加油！' },
    { illustration: '🎉🎶', text: '音乐会上，每个小动物都展示了自己的才艺。大家唱着、跳着，森林里充满了欢声笑语！' }
  ],
  '小兔子的新家': [
    { illustration: '🐰🏠', text: '小兔子要搬新家啦！它收拾好行李，告别了老邻居们，踏上了寻找新家的旅程。' },
    { illustration: '🐰🌲', text: '小兔子来到一棵大树下，"这里真好！"它挖了一个洞，铺上软软的草，这就是它的新家了。' },
    { illustration: '🦊👋', text: '邻居小狐狸来敲门："你好，我是小狐狸，欢迎来到森林！"小兔子开心地说："谢谢你！"' },
    { illustration: '🐿️🌰', text: '小松鼠送来了松果，小鸟送来了野花，大家都来欢迎新邻居。小兔子觉得好温暖！' },
    { illustration: '🐰💖', text: '晚上，小兔子躺在软软的床上，看着窗外的月亮。"新家真好，有这么多好朋友！"' },
    { illustration: '🌈✨', text: '从那以后，小兔子在森林里快乐地生活着，每天都有好朋友来串门，它觉得幸福极了！' }
  ],
  '海洋探险记': [
    { illustration: '🌊🐟', text: '小海豚邀请小乌龟一起去海洋探险！"海底有很多神奇的东西，我们一起去看看吧！"' },
    { illustration: '🐠🌊', text: '他们游过五彩斑斓的珊瑚礁，看到了小丑鱼在海葵里钻来钻去，真有趣！' },
    { illustration: '🐙🐚', text: '一只章鱼从洞穴里探出头来，它有八只触手，还能变色呢！"欢迎来到我的家！"' },
    { illustration: '🐋💙', text: '他们遇到了巨大的蓝鲸，蓝鲸伯伯说："我是世界上最大的动物，但我只吃小小的磷虾。"' },
    { illustration: '🦈🌊', text: '小鲨鱼游过来，它看起来很凶，但其实很友善。"我来带你们去看海底的宝藏！"' },
    { illustration: '💎🌊', text: '海底有闪闪发光的珍珠和美丽的贝壳，小海豚和小乌龟觉得海底世界真是太神奇了！' }
  ],
  '小松鼠的秋天': [
    { illustration: '🐿️🍂', text: '秋天来了，森林里的树叶变得金黄金黄的。小松鼠知道，该准备过冬的粮食了！' },
    { illustration: '🐿️🌰', text: '小松鼠在树林里跳来跳去，收集松果和坚果。"一颗、两颗、三颗..."它把食物藏到树洞里。' },
    { illustration: '🐿️🍁', text: '树叶纷纷落下，小松鼠用叶子把自己裹得暖暖的。"这样就不怕冷了！"' },
    { illustration: '🐻🧡', text: '小熊看见了，也来帮忙。它爬上树，把高处的松果摇下来给小松鼠捡。' },
    { illustration: '❄️🐿️', text: '冬天来了，小松鼠躲在温暖的树洞里，吃着美味的松果，心里感谢帮助过它的朋友们。' },
    { illustration: '🌟💖', text: '小松鼠明白了，勤劳和友谊让它度过了一个温暖的冬天。明年秋天，它也要帮助更多朋友！' }
  ],
  '小企鹅学游泳': [
    { illustration: '🐧❄️', text: '小企鹅住在寒冷的南极。妈妈说："小企鹅，你该学游泳了！"' },
    { illustration: '🐧😰', text: '小企鹅看着冰冷的海水，有点害怕。"水好冷啊，我不敢下去..."' },
    { illustration: '🐧💪', text: '妈妈鼓励它："别怕，我们企鹅是天生的游泳健将！你一定可以的！"' },
    { illustration: '🐧💦', text: '小企鹅鼓起勇气，扑通一声跳进水里。"咦？我在水里会浮起来呢！"' },
    { illustration: '🐧🐟', text: '小企鹅学着妈妈的样子划水，很快就能游得很快了。它还抓到了一条小鱼！' },
    { illustration: '🎉🐧', text: '"太棒了！我学会游泳了！"小企鹅开心极了。原来勇敢尝试，就能做到！' }
  ],
  '森林医生啄木鸟': [
    { illustration: '🌳🩺', text: '啄木鸟是森林里的医生。它每天在树上"笃笃笃"地敲着，给大树治病。' },
    { illustration: '🐛😱', text: '大树生病了，叶子都枯黄了。啄木鸟检查后发现树干里有好多害虫在捣乱！' },
    { illustration: '🪶💪', text: '啄木鸟用尖尖的嘴巴啄开树皮，把害虫一只只揪出来。"看你们还敢不敢欺负大树！"' },
    { illustration: '🌳😊', text: '治好了病，大树又长出了绿油油的叶子。"谢谢你，啄木鸟医生！"' },
    { illustration: '🪶❤️', text: '啄木鸟说："不用谢，帮助大家是我应该做的！"' },
    { illustration: '🌲✨', text: '森林里的大树都健康成长，多亏了勤劳的啄木鸟医生。它是最受欢迎的森林小卫士！' }
  ],
  '小猫钓鱼': [
    { illustration: '🐱🎣', text: '小猫和妈妈一起去河边钓鱼。小猫拿着鱼竿，坐在河边等待鱼儿上钩。' },
    { illustration: '🦋🌸', text: '一只美丽的蝴蝶飞过来，小猫放下鱼竿去追蝴蝶。蝴蝶飞走了，小猫什么也没抓到。' },
    { illustration: '🐸🍀', text: '一只青蛙跳过来，小猫又去追青蛙。青蛙跳进水里，小猫还是什么也没抓到。' },
    { illustration: '🐱🐟', text: '妈妈已经钓了好几条鱼了，小猫的桶里却空空的。"为什么我一条鱼也钓不到呢？"' },
    { illustration: '🐱💪', text: '妈妈说："钓鱼要专心，不能三心二意。"小猫点点头，认真地看着鱼漂。' },
    { illustration: '🐟🎉', text: '终于，鱼漂动了！小猫用力一拉，钓到了一条大鱼！"我做到了！专心做事才能成功！"' }
  ],
  '蝴蝶的变身': [
    { illustration: '🐛🍃', text: '一片叶子上，住着一条小小的毛毛虫。它每天不停地吃叶子，慢慢长大。' },
    { illustration: '🐛💤', text: '有一天，毛毛虫吐出丝，把自己包裹起来，变成了一个茧。它在里面安静地睡着。' },
    { illustration: '茧✨', text: '过了好几天，茧动了一下，接着裂开了一条缝。有什么东西要出来了！' },
    { illustration: '🦋🌟', text: '一只美丽的蝴蝶从茧里飞了出来！它的翅膀五彩斑斓，像一朵会飞的花。' },
    { illustration: '🦋🌸', text: '蝴蝶飞到花丛中，吸食花蜜。"我原来是毛毛虫，现在变成了美丽的蝴蝶，真神奇！"' },
    { illustration: '🌈🦋', text: '蝴蝶在花丛中翩翩起舞，它明白了成长的过程虽然漫长，但结果如此美好！' }
  ],
  '小象的长鼻子': [
    { illustration: '🐘❓', text: '小象有一根长长的鼻子，它觉得自己的鼻子太长了，不好看。' },
    { illustration: '🐘😊', text: '妈妈说："你的鼻子可是宝贝呢！它可以做很多事。"小象不太相信。' },
    { illustration: '🐘💦', text: '天气很热，小象用鼻子吸水，然后喷在身上给自己洗澡。"哇，好凉快！"' },
    { illustration: '🐘🍃', text: '小象用鼻子卷起地上的树叶，送到嘴里吃。"原来鼻子像手一样好用！"' },
    { illustration: '🐘❤️', text: '朋友摔倒了，小象用鼻子把它扶起来。"谢谢你的长鼻子！"' },
    { illustration: '🐘✨', text: '小象终于明白了，每个人都有自己独特的地方。它的长鼻子是它最好的礼物！' }
  ],
  '小狐狸找朋友': [
    { illustration: '🦊🌲', text: '在一片美丽的森林里，住着一只可爱的小狐狸。它有着火红火红的毛，非常漂亮。' },
    { illustration: '🦊😔', text: '小狐狸很聪明，但总觉得森林里的动物们都躲着它。"为什么大家都不愿意和我玩呢？"' },
    { illustration: '🐰👋', text: '有一天，小狐狸鼓起勇气，走向正在采蘑菇的小白兔。"你好，我们能做朋友吗？"' },
    { illustration: '🦊🎀', text: '小狐狸从身后拿出一朵漂亮的野花，送给小白兔。小白兔接过了花，露出了一点点笑容。' },
    { illustration: '🐰🤝', text: '"谢谢你，小狐狸！你真友好！"小白兔说，"我们一起玩吧！"' },
    { illustration: '🌟💕', text: '小狐狸开心极了，它明白了交朋友的秘诀：真诚和善良。从那以后，它成了森林里最受欢迎的小动物！' }
  ],
  '河马的大嘴巴': [
    { illustration: '🦛👄', text: '河马有一张大大的嘴巴，是动物王国里嘴巴最大的。它常常担心自己的嘴巴太大了。' },
    { illustration: '🦛😊', text: '一天，小猴子掉进了河里，它不会游泳，在大声呼救！"救命啊！救命啊！"' },
    { illustration: '🦛💪', text: '河马听到了，立刻跳进水里，用大嘴巴轻轻托起小猴子，把它送到岸边。' },
    { illustration: '🐒❤️', text: '小猴子得救了！它感激地说："谢谢你，河马！你的大嘴巴真棒！"' },
    { illustration: '🦛✨', text: '河马这才明白，自己的大嘴巴不是缺点，而是可以帮助别人的优点！' },
    { illustration: '🌈💖', text: '从那以后，河马不再为大嘴巴烦恼了。它用大嘴巴帮助了很多朋友，成了河里的大英雄！' }
  ],
  '小猴子吃桃子': [
    { illustration: '🐒🍑', text: '小猴子最喜欢吃桃子了！有一天，它发现了一棵结满桃子的树。' },
    { illustration: '🐒🤔', text: '小猴子摘了一个桃子，咬了一口，就把桃子扔了。"不好吃，我要找更甜的！"' },
    { illustration: '🐒🏃', text: '小猴子又摘了一个，吃一口又扔了。就这样，它扔了很多桃子。' },
    { illustration: '🐒😓', text: '最后，树上的桃子都被它摘光了，它却一个也没吃完。小猴子肚子饿得咕咕叫。' },
    { illustration: '👴🌳', text: '老猴子爷爷看见了，说："孩子，做事要专注，不能三心二意。你浪费了这么多桃子！"' },
    { illustration: '🐒💖', text: '小猴子明白了道理，从此它吃东西时都会认真品尝，不再浪费食物了。' }
  ],
  '小乌龟赛跑': [
    { illustration: '🐢🐰', text: '森林里要举办跑步比赛，骄傲的兔子说："我跑得最快，冠军肯定是我的！"' },
    { illustration: '🐢💪', text: '小乌龟慢慢地说："我也要参加比赛！"兔子哈哈大笑："你这么慢，怎么可能赢？"' },
    { illustration: '🐰😴', text: '比赛开始了，兔子跑得飞快，很快就把乌龟甩在后面。它想："乌龟那么慢，我睡一觉再跑也来得及。"' },
    { illustration: '🐢💦', text: '小乌龟一步一步慢慢爬，它不休息，不停歇，一直往前爬。"我不能放弃！"' },
    { illustration: '🐢🎉', text: '当兔子醒来时，发现乌龟已经到达终点了！大家都为小乌龟欢呼！' },
    { illustration: '🌟🐢', text: '小乌龟赢了！它用行动告诉大家：坚持就是胜利，骄傲会让人失败。' }
  ],
  '小蜜蜂采蜜': [
    { illustration: '🐝🌸', text: '春天来了，花园里开满了五颜六色的花朵。小蜜蜂要开始工作了！' },
    { illustration: '🐝✈️', text: '小蜜蜂飞到一朵花上，用长长的嘴巴吸取花蜜。"嗡嗡嗡，花儿真香！"' },
    { illustration: '🐝🍯', text: '小蜜蜂把花蜜存在肚子里的蜜囊中，飞回蜂巢，把花蜜变成甜甜的蜂蜜。' },
    { illustration: '🐝🏠', text: '蜂巢里，成千上万只蜜蜂一起工作。有的采蜜，有的酿蜜，有的照顾小蜜蜂。' },
    { illustration: '🐝❤️', text: '小蜜蜂虽然很累，但它很开心。"我们的蜂蜜可以给人们带来甜蜜，我们的工作很有意义！"' },
    { illustration: '🍯✨', text: '勤劳的小蜜蜂教会我们：努力工作，团结合作，才能创造美好的生活！' }
  ],
  '小海龟回家': [
    { illustration: '🐢🌊', text: '沙滩上，一只小海龟从蛋里孵化出来了。它要爬向大海，开始自己的旅程。' },
    { illustration: '🐢🏃', text: '小海龟努力地爬着，沙子很烫，路程很远，但它没有放弃。"大海，我来了！"' },
    { illustration: '🐦😱', text: '天空中，海鸟在盘旋，它们想吃掉小海龟。小海龟害怕极了，拼命向前爬。' },
    { illustration: '🐢💦', text: '终于，小海龟爬到了海边！它跳进海里，游向大海深处。"我得救了！"' },
    { illustration: '🐢🐟', text: '在海里，小海龟遇到了很多好朋友，有彩色的鱼，有漂亮的珊瑚。' },
    { illustration: '🏠🐢', text: '小海龟终于找到了自己的家。它明白了，只要不放弃，就能到达目的地！' }
  ],
  '森林运动会': [
    { illustration: '🌲🏅', text: '今天森林里举办运动会！小动物们都来参加，有跑步、跳高、游泳等项目。' },
    { illustration: '🐆🏃', text: '跑步比赛开始了！豹子跑得最快，像一阵风一样冲过了终点。"我是冠军！"' },
    { illustration: '🦘🏆', text: '跳高比赛中，袋鼠跳得最高。它轻轻一跃，就跳过了最高的横杆！' },
    { illustration: '🏊🐟', text: '游泳比赛最精彩，小鱼和青蛙你追我赶，最后小鱼赢了第一名。' },
    { illustration: '🎉❤️', text: '运动会结束了，大家都玩得很开心。重要的不是输赢，而是参与和友谊！' },
    { illustration: '🤝✨', text: '小动物们互相祝贺，约定明年再来比赛。运动会让大家更加团结友爱了！' }
  ],
  '小鸟学唱歌': [
    { illustration: '🐦🎵', text: '小鸟长大了，妈妈要教它唱歌。"叽叽喳喳，像这样唱！"' },
    { illustration: '🐦😔', text: '小鸟试着唱了几声，但声音不好听。"妈妈，我不会唱歌..."' },
    { illustration: '🐦💪', text: '妈妈鼓励它："没关系，多练习就会了。每天早晨来跟我学！"' },
    { illustration: '🌅🐦', text: '小鸟每天早早起床练习，一天、两天、一个月过去了...它的歌声越来越好听。' },
    { illustration: '🐦🎤', text: '终于，小鸟的歌声变得清脆悦耳，像银铃一样美妙。森林里的小动物都来听它唱歌。' },
    { illustration: '🌟💕', text: '小鸟明白了，只要坚持练习，就能学会任何本领。努力的孩子最棒！' }
  ],
  '小马过河': [
    { illustration: '🐴🌾', text: '小马长大了，妈妈让它独自把粮食送到河对岸的村子里。小马高兴地出发了。' },
    { illustration: '🐴🌊', text: '来到河边，小马停住了。河水哗哗地流着，它不知道河水深不深，能不能过去。' },
    { illustration: '🐂😮', text: '老牛说："水很浅，刚没过小腿，能过去。"小马准备过河。' },
    { illustration: '🐿️😱', text: '小松鼠急忙说："别过河！水很深，会淹死的！"小马不知道该听谁的。' },
    { illustration: '🐴💡', text: '小马回家问妈妈。妈妈说："光听别人说不行，要自己动脑筋，亲自试一试。"' },
    { illustration: '🐴✨', text: '小马回到河边，小心地试着过河。原来河水既不像老牛说的那么浅，也不像松鼠说的那么深。它顺利地过了河！' }
  ],
  // ===== 科普类 =====
  '人体奥秘': [
    { illustration: '👶🔬', text: '欢迎来到人体奥秘的世界！我们的身体就像一个超级精密的机器，每时每刻都在工作！' },
    { illustration: '❤️💓', text: '这是我们的心脏，它只有拳头那么大，却非常有力！心脏每天要跳动约10万次，把血液输送到全身。咚咚咚，你听到心跳的声音了吗？' },
    { illustration: '🧠💭', text: '大脑是我们身体的"总指挥"！它只有1.4公斤重，却掌管着我们的思考、记忆、学习。你的大脑里有大约1000亿个神经细胞呢！' },
    { illustration: '👀🌈', text: '我们的眼睛就像一台神奇的照相机！它能看到几百万种颜色，还能在黑暗和光明中自动调节。眨眼的时候，眼睛就在给自己洗澡哦！' },
    { illustration: '👂🎵', text: '耳朵不仅能听到声音，还帮助我们保持平衡。耳朵里有一万多个小小的毛细胞，它们把声音变成信号传给大脑。' },
    { illustration: '🦷🍎', text: '牙齿是我们身体里最坚硬的部分！小朋友有20颗乳牙，长大后会有32颗恒牙。记得每天刷牙两次，保护我们的牙齿卫士哦！' },
    { illustration: '🦴🏃', text: '我们的身体有206块骨头！它们支撑着我们的身体，保护着重要的器官。最小的骨头在耳朵里，最大的骨头是大腿骨。' },
    { illustration: '💪✨', text: '肌肉让我们能够运动！身体里有600多块肌肉。微笑一下，你就用了17块肌肉哦！' },
    { illustration: '🫁🌬️', text: '肺是我们呼吸的"气球"！我们每天大约呼吸2万次，吸入氧气，呼出二氧化碳。' },
    { illustration: '🧪🔬', text: '我们的身体真是太神奇了！希望你保持好奇心，好好爱护自己的身体，它会陪伴你一辈子哦！' }
  ],
  '太空探险记': [
    { illustration: '🚀🌍', text: '小朋友，你有没有仰望过夜空？那些闪烁的小星星，其实都是遥远的太阳！让我们一起去探索神奇的太空吧！' },
    { illustration: '🌙⭐', text: '看！那是我们的月亮。月亮上没有空气，也没有水，所以没有生命。但是，月亮上有许多有趣的山和坑，叫做环形山。' },
    { illustration: '🔴🪐', text: '这是火星，因为它红红的，所以叫"火星"。火星上最高的山比地球上的珠穆朗玛峰还要高三倍呢！' },
    { illustration: '🪐💫', text: '哇！那是土星！看它漂亮的光环，其实是无数的小冰块和小石头组成的。土星可以装下700多个地球呢！' },
    { illustration: '☀️🌟', text: '那是我们的太阳！太阳是一颗恒星，它一直在发光发热。太阳光到达地球需要8分钟呢！' },
    { illustration: '🌌✨', text: '宇宙中有无数的星星和行星。也许有一天，你也会成为一名宇航员，去探索这些神秘的星球！' }
  ],
  '恐龙世界': [
    { illustration: '🦕🌴', text: '很久很久以前，在6500万年前的地球上，生活着一群神奇的动物——恐龙！' },
    { illustration: '🦖🌿', text: '霸王龙是恐龙之王！它有尖尖的牙齿，强壮的后腿，是最凶猛的肉食恐龙。' },
    { illustration: '🦕🌳', text: '腕龙是一种超级大的恐龙，它的脖子像长颈鹿一样长，可以吃到很高很高的树叶。' },
    { illustration: '🥚👶', text: '恐龙是从蛋里孵出来的！恐龙妈妈会在沙土里挖一个坑，把蛋生在里面。' },
    { illustration: '☄️😱', text: '可惜，大约6500万年前，一颗巨大的陨石撞击了地球，恐龙们从此消失了。' },
    { illustration: '🦴🔬', text: '科学家们通过研究恐龙化石，了解恐龙的生活。也许有一天，你也能成为一名古生物学家！' }
  ],
  '四季的变化': [
    { illustration: '🌸🌱', text: '春天来了！小草从土里探出头，树木长出了嫩绿的新芽，花儿都开放了。' },
    { illustration: '☀️🏊', text: '夏天到了！太阳火辣辣的，我们可以吃冰激凌、游泳、捉知了。' },
    { illustration: '🍂🍁', text: '秋天来了！树叶变成了金黄色，纷纷落下。果园里的水果都成熟了。' },
    { illustration: '❄️⛄', text: '冬天到了！天空飘起了雪花，我们堆雪人、打雪仗，真好玩！' },
    { illustration: '🔄❤️', text: '四季就这样循环变化着。每个季节都有自己的美丽，我们要用心感受大自然的变化！' },
    { illustration: '🌈✨', text: '大自然真神奇！让我们珍惜每一个季节，享受大自然的馈赠吧！' }
  ],
  '昆虫王国': [
    { illustration: '🐛🐜', text: '欢迎来到昆虫王国！昆虫是地球上数量最多的动物，它们有着神奇的本领。' },
    { illustration: '🦋🌸', text: '蝴蝶是昆虫王国里最美丽的居民。它们有五彩斑斓的翅膀，在花丛中翩翩起舞。' },
    { illustration: '🐝🍯', text: '蜜蜂是最勤劳的昆虫。它们采花蜜、酿蜂蜜，为我们带来甜蜜的食物。' },
    { illustration: '🐜💪', text: '蚂蚁虽然很小，但力气很大。它们团结合作，可以搬运比自己重很多倍的东西。' },
    { illustration: '🦗🎵', text: '蟋蟀是昆虫音乐家，夏天的夜晚，它们在草丛里唱着美妙的歌。' },
    { illustration: '🌟🔍', text: '昆虫王国里还有很多秘密等着你去发现。下次看到小昆虫，不妨仔细观察一下！' }
  ],
  '植物的秘密': [
    { illustration: '🌱🌍', text: '植物是地球上最重要的生物之一！它们为我们提供氧气、食物和美丽的风景。' },
    { illustration: '🌻☀️', text: '植物通过光合作用，用阳光、水和空气制造自己的食物。它们是地球的"氧气工厂"！' },
    { illustration: '🌸🦋', text: '花儿开放是为了吸引昆虫来传播花粉。花谢之后，就会结出果实和种子。' },
    { illustration: '🌳🏠', text: '大树是很多动物的家。小鸟在树上筑巢，松鼠在树洞里储存粮食。' },
    { illustration: '🍃🍂', text: '秋天，树叶会变黄变红，然后落下。这是大树在为冬天保存能量。' },
    { illustration: '🌱✨', text: '植物的生命力真神奇！一粒小小的种子，可以长成参天大树。让我们爱护植物，保护绿色家园！' }
  ],
  '太阳系家族': [
    { illustration: '☀️🌌', text: '太阳系是一个大家庭，太阳是家长，围绕着它旋转的有八大行星。' },
    { illustration: '🌍🏠', text: '地球是我们的家，它是唯一有生命存在的行星。有海洋、陆地、空气和水。' },
    { illustration: '🔴🪐', text: '火星是红色的行星，科学家们正在研究它，也许未来人类可以在火星上生活！' },
    { illustration: '🪐💍', text: '土星有漂亮的光环，那是由冰块和岩石组成的。土星是太阳系第二大行星。' },
    { illustration: '🌙💫', text: '月亮是地球的好朋友，它围着地球转，给我们带来夜晚的光明。' },
    { illustration: '🚀🌟', text: '太阳系真神奇！小朋友，好好学习，将来也许你能驾驶飞船去探索太空呢！' }
  ],
  // ===== 情感类 =====
  '爱的抱抱': [
    { illustration: '👧❤️', text: '小雨今天不太开心。她坐在门口，低着头不说话。' },
    { illustration: '👩❤️', text: '妈妈走过来，什么也没说，只是轻轻地抱住了小雨。' },
    { illustration: '👧😊', text: '小雨在妈妈的怀里，感到好温暖。所有的不开心都飞走了。' },
    { illustration: '💕✨', text: '"妈妈，为什么抱抱这么神奇？"小雨问。妈妈笑着说："因为抱抱里藏着爱！"' },
    { illustration: '👨‍👩‍👧💖', text: '爸爸回来了，小雨跑过去给了爸爸一个大大的拥抱。"我爱你们！"' },
    { illustration: '🌟🤗', text: '小雨明白了，爱是可以传递的。一个拥抱，就能让人感到幸福。她要把拥抱送给更多的人！' }
  ],
  '我的好朋友': [
    { illustration: '👧👦', text: '小明是小红最好的朋友。他们一起上学，一起玩耍，形影不离。' },
    { illustration: '👧📚', text: '有一天，小红生病了，不能去上学。她很想小明，也很担心落下功课。' },
    { illustration: '👦🎒', text: '放学后，小明来到小红家，把今天的功课讲给她听，还带来了她最爱吃的糖果。' },
    { illustration: '👧❤️', text: '小红感动地说："谢谢你，小明！"小明笑着说："好朋友就是要互相帮助！"' },
    { illustration: '🌈🤝', text: '小红病好了，又和小明一起快乐地玩耍。他们的友谊更加深厚了。' },
    { illustration: '💖✨', text: '真正的朋友会在你需要的时候陪伴你、帮助你。珍惜友谊，友谊会让我们更快乐！' }
  ],
  '我会关心别人': [
    { illustration: '👧🏠', text: '小芳是一个善良的小女孩。她总是关心身边的人，帮助他们解决困难。' },
    { illustration: '👴🚶', text: '一天，小芳看到邻居老爷爷走路很慢，还拿着重重的菜篮子。' },
    { illustration: '👧💪', text: '小芳跑过去说："爷爷，我来帮您提菜篮子！"老爷爷开心地笑了。' },
    { illustration: '👦😢', text: '在公园里，小芳看到一个小弟弟在哭，原来是气球飞走了。' },
    { illustration: '👧🎈', text: '小芳用自己的零花钱给小弟弟买了一个新气球。小弟弟开心地笑了。' },
    { illustration: '❤️✨', text: '关心别人，帮助别人，自己也会感到快乐。小芳成了大家最喜欢的小朋友！' }
  ],
  '我不怕黑': [
    { illustration: '👦🌙', text: '小强很怕黑。每天晚上睡觉，他都要开着小夜灯，还要妈妈陪在身边。' },
    { illustration: '👦😰', text: '一天晚上，家里突然停电了。四周黑漆漆的，小强害怕极了！' },
    { illustration: '💡✨', text: '妈妈点燃了蜡烛，温暖的烛光照亮了房间。"看，黑暗中也有光！"' },
    { illustration: '👦💪', text: '妈妈说："黑暗其实不可怕，可怕的是你心里的恐惧。勇敢地面对它！"' },
    { illustration: '👦🌟', text: '小强慢慢平静下来，他发现黑暗中也有美好的东西——窗外的星星在眨眼呢！' },
    { illustration: '🎉✨', text: '从那以后，小强不再怕黑了。他明白了，勇敢的心能战胜一切恐惧！' }
  ],
  '妈妈我爱你': [
    { illustration: '👧💕', text: '今天是母亲节，小丽想给妈妈一个特别的惊喜。' },
    { illustration: '👧🎨', text: '小丽画了一幅画，画的是妈妈和她手牵手在公园散步。画上写着"妈妈我爱你"。' },
    { illustration: '👧🍳', text: '小丽还学着做了早餐，虽然煎蛋有点糊，但她很用心。' },
    { illustration: '👩😊', text: '妈妈醒来，看到了画和早餐，感动得流下了眼泪。"谢谢你，宝贝！"' },
    { illustration: '👧🤗', text: '小丽紧紧抱住妈妈："妈妈，您辛苦了！我永远爱您！"' },
    { illustration: '💖✨', text: '妈妈的爱是最伟大的爱。我们要好好爱妈妈，让她每天都开心！' }
  ],
  '爸爸真棒': [
    { illustration: '👦👨', text: '小明的爸爸是一名消防员。小明觉得爸爸是世界上最棒的爸爸！' },
    { illustration: '👨🚒', text: '每当有火灾，爸爸总是冲在最前面，保护大家的安全。小明很崇拜爸爸。' },
    { illustration: '👦🏠', text: '爸爸工作很忙，但只要有时间，就会陪小明踢足球、讲故事。' },
    { illustration: '👨❤️', text: '有一天，爸爸在救火时受了伤。小明心疼地说："爸爸，您辛苦了！"' },
    { illustration: '👨👦', text: '爸爸笑着说："保护大家是我的责任。你也要做一个勇敢、负责任的人！"' },
    { illustration: '💪✨', text: '小明的爸爸真棒！每个爸爸都是孩子心中的英雄。让我们对爸爸说：谢谢您，我爱您！' }
  ],
  '分享的快乐': [
    { illustration: '👧🍪', text: '小红有一盒美味的饼干，她想自己一个人慢慢地吃。' },
    { illustration: '👧🤔', text: '这时，几个小朋友走过来，他们看着小红的饼干，都很想吃。' },
    { illustration: '👧😊', text: '小红想了想，把饼干分给了大家。"来，我们一起吃！"' },
    { illustration: '👧👦🍪', text: '小朋友们开心地吃着饼干，有说有笑。小红发现，和大家一起吃比自己一个人吃更快乐！' },
    { illustration: '❤️✨', text: '小红明白了，分享不是失去，而是获得更多的快乐。分享让友谊更美好！' },
    { illustration: '🤝🌈', text: '从那以后，小红有好东西都会和朋友分享。她成了大家最喜欢的小伙伴！' }
  ],
  '我很勇敢': [
    { illustration: '👦💪', text: '小刚是个胆小的男孩，他怕黑、怕高、怕虫子...什么都怕。' },
    { illustration: '👦😰', text: '一天，小刚看到一只小猫被困在树上，它害怕地喵喵叫。' },
    { illustration: '👦🤔', text: '小刚很想救小猫，但他怕高。"我该怎么办？"' },
    { illustration: '👦💪', text: '小刚深吸一口气，鼓起勇气爬上了树。他抱着小猫，小心地爬下来。' },
    { illustration: '🐱❤️', text: '小猫得救了！小刚的爸爸妈妈都为他鼓掌："你真勇敢！"' },
    { illustration: '🏆✨', text: '小刚明白了，勇敢不是不害怕，而是虽然害怕但还是会去做对的事情。他是个勇敢的孩子！' }
  ],
  '我很特别': [
    { illustration: '👧🌸', text: '小美总觉得自己很普通，没有什么特别的。她羡慕别人比自己漂亮、聪明、能干。' },
    { illustration: '👩❤️', text: '妈妈知道后，拿出了一面镜子，让小美仔细看看自己。' },
    { illustration: '👧👀', text: '"看看你的眼睛，多明亮！看看你的笑容，多温暖！看看你的心，多善良！"' },
    { illustration: '👧😊', text: '小美笑了。她发现，自己也有很多优点。她善良、爱帮助人、会画画...这些都是她的特别之处。' },
    { illustration: '✨❤️', text: '妈妈说："每个人都是独一无二的，你就是你，很特别的你！"' },
    { illustration: '🌟🌈', text: '小美明白了，不需要和别人比较。做最好的自己，就是最特别的！' }
  ],
  '诚实最珍贵': [
    { illustration: '👦🏺', text: '小华在玩耍时，不小心打碎了妈妈最喜欢的花瓶。' },
    { illustration: '👦😰', text: '小华很害怕，心想：如果妈妈知道了，会不会生气呢？要不要说是猫打破的？' },
    { illustration: '👦🤔', text: '小华想了很久，最后决定告诉妈妈真相。他不想做一个撒谎的孩子。' },
    { illustration: '👩😊', text: '妈妈听了，不但没有生气，还摸了摸小华的头。"花瓶碎了没关系，但你诚实地承认错误，妈妈很高兴！"' },
    { illustration: '👦❤️', text: '小华松了一口气。他明白了，诚实比什么都重要。' },
    { illustration: '💎✨', text: '诚实是最珍贵的品质。做一个诚实的孩子，会赢得大家的信任和尊重！' }
  ],
  '我长大了': [
    { illustration: '👧🎂', text: '今天是小兰的生日，她7岁了！她觉得自己长大了。' },
    { illustration: '👧👔', text: '小兰发现，以前的衣服变短了，鞋子变小了。"我长大了！"' },
    { illustration: '👧📚', text: '小兰可以自己读书、自己系鞋带、自己收拾书包了。"我能做很多事了！"' },
    { illustration: '👧🏠', text: '小兰还学会了帮妈妈做家务，扫地、擦桌子、洗碗。"我可以帮妈妈分担了！"' },
    { illustration: '👩❤️', text: '妈妈说："长大不仅是长高，更是学会独立、学会关心别人。"' },
    { illustration: '🌟✨', text: '小兰开心地说："我长大了，我要做一个懂事的好孩子！"' }
  ],
  // ===== 童话类 =====
  '灰姑娘': [
    { illustration: '👧😢', text: '从前，有一个善良美丽的女孩，名叫灰姑娘。她被继母和姐姐们欺负，每天要做很多家务。' },
    { illustration: '✨👗', text: '王子要举办舞会，仙女教母用魔法把灰姑娘变成了美丽的公主，还给她变出了水晶鞋。' },
    { illustration: '💃🏰', text: '灰姑娘在舞会上和王子跳舞，他们是那么般配。但魔法只能维持到午夜。' },
    { illustration: '👠🏃', text: '午夜的钟声响了，灰姑娘匆忙离开，不小心跑掉了一只水晶鞋。' },
    { illustration: '👸❤️', text: '王子拿着水晶鞋寻找它的主人。最后，只有灰姑娘穿得下这只鞋！' },
    { illustration: '👑✨', text: '灰姑娘和王子结婚了，从此过上了幸福的生活。善良的人终会有好报！' }
  ],
  '白雪公主': [
    { illustration: '👸🍎', text: '美丽的白雪公主被恶毒的皇后赶出城堡，她在森林里遇到了七个善良的小矮人。' },
    { illustration: '👵🍎', text: '恶毒的皇后扮成老婆婆，给白雪公主一个有毒的苹果。白雪公主吃了一口，就昏睡过去了。' },
    { illustration: '王子❤️', text: '邻国的王子路过，看到美丽的白雪公主，爱上了她。他轻轻地吻了她。' },
    { illustration: '👸✨', text: '神奇的事情发生了！白雪公主醒了过来，毒苹果的魔力消失了。' },
    { illustration: '👑🎉', text: '王子带着白雪公主回到城堡，他们举行了盛大的婚礼，幸福地生活在一起。' },
    { illustration: '💖✨', text: '善良和美丽的心灵，终究会战胜邪恶。白雪公主的故事告诉我们：要做善良的人！' }
  ],
  '小红帽': [
    { illustration: '👧🏠', text: '小红帽要去看望生病的奶奶，妈妈让她走大路，不要和陌生人说话。' },
    { illustration: '🐺🌲', text: '路上，小红帽遇到了一只大灰狼。狡猾的狼骗小红帽去采花，自己跑去奶奶家。' },
    { illustration: '🐺👵', text: '大灰狼把奶奶吞进肚子，又扮成奶奶的样子躺在床上，等着小红帽。' },
    { illustration: '👧😰', text: '小红帽来到奶奶家，发现"奶奶"的样子很奇怪。"奶奶，你的耳朵怎么这么大？"' },
    { illustration: '🐺😱', text: '"是为了听得更清楚！"狼跳起来要吃小红帽。这时，猎人叔叔冲进来救了她们！' },
    { illustration: '👧✨', text: '小红帽和奶奶得救了！她明白了：不能轻信陌生人，要学会保护自己。' }
  ],
  '三只小猪': [
    { illustration: '🐷🏠', text: '三只小猪长大了，要自己盖房子住。猪大哥盖了草房，猪二哥盖了木房，猪小弟盖了砖房。' },
    { illustration: '🐺💨', text: '大灰狼来了！它吹了一口气，就把草房吹倒了。猪大哥逃到了猪二哥的木房。' },
    { illustration: '🐺💨', text: '大灰狼又吹了一口气，木房也被吹倒了！两只小猪逃到了猪小弟的砖房。' },
    { illustration: '🐺😤', text: '大灰狼吹呀吹，砖房纹丝不动。它想从烟囱钻进去，结果掉进热水锅里烫跑了！' },
    { illustration: '🐷🎉', text: '三只小猪高兴地欢呼起来！猪小弟的砖房救了他们。' },
    { illustration: '💪✨', text: '三只小猪明白了：做事要认真踏实，不能偷懒。辛苦盖的房子才能保护好自己！' }
  ],
  '丑小鸭': [
    { illustration: '🥚🐥', text: '鸭妈妈孵出了一群小鸭子，其中有一只又大又丑。大家都叫他"丑小鸭"，没有人喜欢他。' },
    { illustration: '🐥😢', text: '丑小鸭很难过，他离开了家，独自流浪。冬天来了，他又冷又饿。' },
    { illustration: '🐥🌱', text: '春天来了，丑小鸭看到水中的倒影——他变成了一只美丽的天鹅！' },
    { illustration: '🦢✨', text: '原来，丑小鸭不是鸭子，而是一只小天鹅！他终于找到了自己的家人。' },
    { illustration: '🦢💖', text: '丑小鸭变成了最美的天鹅，他在湖中优雅地游泳，感到无比幸福。' },
    { illustration: '🌈✨', text: '每个人都有自己的闪光点，不要因为别人的眼光而否定自己。相信自己，你也是最美的！' }
  ],
  '龟兔赛跑': [
    { illustration: '🐢🐰', text: '骄傲的兔子嘲笑乌龟跑得慢。乌龟说："我们来比赛跑步吧！"' },
    { illustration: '🐰💨', text: '比赛开始了！兔子跑得飞快，一下子就把乌龟甩在后面很远。' },
    { illustration: '🐰😴', text: '兔子想："乌龟那么慢，我睡一觉再跑也来得及。"它在树下睡着了。' },
    { illustration: '🐢💦', text: '乌龟一步一步地爬，它不休息，不停歇，一直坚持向前。' },
    { illustration: '🐢🎉', text: '当兔子醒来时，乌龟已经到达终点了！大家都为乌龟欢呼！' },
    { illustration: '🏆✨', text: '乌龟赢了！这个故事告诉我们：骄傲使人落后，坚持就是胜利！' }
  ],
  '神笔马良': [
    { illustration: '👦🖌️', text: '马良是一个爱画画的穷孩子，但他买不起笔。一天晚上，一位老爷爷送给他一支神笔。' },
    { illustration: '🖌️🐄', text: '马良发现，神笔画的东西都会变成真的！他画了一头牛，牛就活了！' },
    { illustration: '👦❤️', text: '马良用神笔帮助穷苦的百姓。他画粮食、画衣服、画房子，大家都感激他。' },
    { illustration: '👑😰', text: '贪心的皇帝知道了，把马良抓起来，逼他画金山银山。' },
    { illustration: '🖌️🌊', text: '马良画了一片大海，皇帝坐船去金山。马良又画了大风浪，把贪心的皇帝冲走了！' },
    { illustration: '🌈✨', text: '马良回到了百姓身边，继续用神笔帮助大家。善良的力量是最强大的！' }
  ],
  '孔融让梨': [
    { illustration: '👦🏠', text: '孔融是一个聪明懂事的孩子。有一天，爸爸买了一筐梨回家。' },
    { illustration: '🍐🍐', text: '哥哥们都在挑大梨，只有孔融挑了一个最小的。爸爸问："你为什么拿最小的？"' },
    { illustration: '👦😊', text: '孔融说："我年纪小，应该吃小的。哥哥们年纪大，应该吃大的。"' },
    { illustration: '👨❤️', text: '爸爸听了很感动："你真是个懂事的好孩子！懂得谦让是美德！"' },
    { illustration: '🤝✨', text: '哥哥们也被孔融感动了，大家一起开心地吃着梨。' },
    { illustration: '💖✨', text: '孔融让梨的故事告诉我们：要懂得谦让，尊敬兄长，关爱他人。这是中华民族的传统美德！' }
  ]
}

const route = useRoute()
const router = useRouter()

const book = ref(null)
const child = ref(null)
const startTime = ref(null)
const readingDuration = ref(0)
const currentPage = ref(1)
const totalPages = ref(20)
const pageTurnCount = ref(0)
const pageStayTimes = ref([])
const lastPageTurnTime = ref(null)
const showExitConfirm = ref(false)
const isSaved = ref(false)

// 互动功能状态
const isBookmarked = ref(false)
const bookmarkCount = ref(0)
const annotationCount = ref(0)
const voiceRecordCount = ref(0)
const showAnnotationDialog = ref(false)
const annotationText = ref('')
const annotations = ref([])
const showAnnotationList = ref(false)
const isRecording = ref(false)
const recordingTime = ref(0)
let recordingTimer = null
let mediaRecorder = null
let audioChunks = []

let timer = null

const readingTips = [
  '可以和孩子讨论画面中的细节，培养观察力',
  '试着用不同的声音演绎不同的角色',
  '问问孩子"你觉得接下来会发生什么？"',
  '鼓励孩子描述他们最喜欢的画面',
  '可以暂停一下，让孩子预测故事发展',
  '读完后可以问问孩子学到了什么',
  '让孩子尝试复述故事的主要内容',
  '引导孩子关注角色的情绪变化'
]

const currentTip = ref(readingTips[0])

const formattedDuration = computed(() => {
  const minutes = Math.floor(readingDuration.value / 60)
  const seconds = readingDuration.value % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

const progressPercent = computed(() => {
  return Math.round((currentPage.value / totalPages.value) * 100)
})

const getPageStyle = computed(() => {
  const gradients = {
    '动物': 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    '科普': 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
    '情感': 'linear-gradient(135deg, #ffeaa7 0%, #ffb88c 100%)',
    '童话': 'linear-gradient(135deg, #f5e6d3 0%, #d4a574 100%)'
  }
  return {
    background: gradients[book.value?.category] || 'linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%)'
  }
})

// 根据书名获取故事内容
const getStoryPages = (bookTitle, bookCategory) => {
  // 直接查找完整书名
  if (storyLibrary[bookTitle]) {
    return storyLibrary[bookTitle]
  }

  // 按关键词匹配
  for (const [key, pages] of Object.entries(storyLibrary)) {
    if (bookTitle.includes(key) || key.includes(bookTitle)) {
      return pages
    }
  }

  // 根据分类返回默认故事
  const categoryDefault = {
    '动物': storyLibrary['小熊的冒险'],
    '科普': storyLibrary['人体奥秘'],
    '情感': storyLibrary['爱的抱抱'],
    '童话': storyLibrary['灰姑娘']
  }
  return categoryDefault[bookCategory] || storyLibrary['小熊的冒险']
}

// 生成足够页数的内容
const generatePagesForBook = (bookTitle, bookCategory, totalPg) => {
  const storyPages = getStoryPages(bookTitle, bookCategory)
  const pages = [...storyPages]

  // 扩展内容
  const extraPages = [
    { illustration: '🔍📖', text: '继续阅读，探索更多精彩内容！' },
    { illustration: '💡✨', text: '阅读让我们变得更聪明！' },
    { illustration: '🌟📚', text: '书本里藏着无限的知识和乐趣！' },
    { illustration: '🌈🎯', text: '你真是一个爱读书的好孩子！' }
  ]

  let extraIndex = 0
  while (pages.length < totalPg) {
    pages.push(extraPages[extraIndex % extraPages.length])
    extraIndex++
  }

  return pages.slice(0, totalPg)
}

const currentPageData = computed(() => {
  // 如果book还没加载，返回默认内容
  if (!book.value) {
    return { illustration: '📖✨', text: '正在加载故事内容...' }
  }
  const pages = generatePagesForBook(book.value.title, book.value.category, totalPages.value)
  return pages[currentPage.value - 1] || pages[0] || { illustration: '📖', text: '加载中...' }
})

const startTimer = () => {
  startTime.value = new Date()
  lastPageTurnTime.value = Date.now()
  timer = setInterval(() => {
    readingDuration.value++
    if (readingDuration.value % 30 === 0) {
      currentTip.value = readingTips[Math.floor(Math.random() * readingTips.length)]
    }
  }, 1000)
}

const stopTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const recordPageStay = () => {
  if (lastPageTurnTime.value) {
    const stayTime = Math.round((Date.now() - lastPageTurnTime.value) / 1000)
    pageStayTimes.value.push(stayTime)
  }
  lastPageTurnTime.value = Date.now()
}

const prevPage = () => {
  if (currentPage.value > 1) {
    recordPageStay()
    currentPage.value--
    pageTurnCount.value++
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    recordPageStay()
    currentPage.value++
    pageTurnCount.value++
  }
}

const confirmExit = () => {
  showExitConfirm.value = true
}

const exitReading = async () => {
  await saveReadingLog(false)
  router.push('/app/reading-logs')
}

const finishReading = async () => {
  recordPageStay()
  await saveReadingLog(true)
  ElMessage.success({ message: '🎉 太棒了！阅读完成！', duration: 2000 })
  router.push('/app/reading-logs')
}

// ============ 互动功能 ============

// 收藏/取消收藏
const toggleBookmark = async () => {
  try {
    if (isBookmarked.value) {
      await removeBookmark(child.value?.id, book.value?.id)
      isBookmarked.value = false
      bookmarkCount.value = Math.max(0, bookmarkCount.value - 1)
      ElMessage.success('已取消收藏')
    } else {
      await addBookmark({
        childId: child.value?.id,
        bookId: book.value?.id,
        bookmarkType: 1,
        note: `第${currentPage.value}页收藏`
      })
      isBookmarked.value = true
      bookmarkCount.value++
      ElMessage.success('❤️ 收藏成功！')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 检查收藏状态
const checkBookmarkStatus = async () => {
  try {
    const res = await checkBookmark(child.value?.id, book.value?.id)
    isBookmarked.value = res.data || false
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 打开批注弹窗
const openAnnotationDialog = () => {
  annotationText.value = ''
  showAnnotationDialog.value = true
}

// 提交批注
const submitAnnotation = async () => {
  if (!annotationText.value.trim()) {
    ElMessage.warning('请输入批注内容')
    return
  }
  try {
    await addAnnotation({
      childId: child.value?.id,
      bookId: book.value?.id,
      pageNum: currentPage.value,
      annotationType: 1, // 文字批注
      content: annotationText.value
    })
    annotationCount.value++
    showAnnotationDialog.value = false
    annotationText.value = ''
    ElMessage.success('📝 批注添加成功！')
    loadAnnotations()
  } catch (error) {
    console.error('添加批注失败:', error)
    ElMessage.error('添加失败')
  }
}

// 加载批注列表
const loadAnnotations = async () => {
  try {
    const res = await getBookAnnotations(child.value?.id, book.value?.id)
    annotations.value = res.data || []
    // 统计批注和录音数量
    annotationCount.value = annotations.value.filter(a => a.annotationType === 1).length
    voiceRecordCount.value = annotations.value.filter(a => a.annotationType === 2).length
  } catch (error) {
    console.error('加载批注失败:', error)
  }
}

// 播放录音
const playAudio = (base64Audio) => {
  const audio = new Audio(base64Audio)
  audio.play()
}

// 删除批注
const handleDeleteAnnotation = async (id) => {
  try {
    await deleteAnnotation(id)
    annotationCount.value = Math.max(0, annotationCount.value - 1)
    loadAnnotations()
    ElMessage.success('已删除')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 录音数据存储
const audioRecords = ref([])

// 开始录音
const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (e) => {
      audioChunks.push(e.data)
    }

    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm' })
      // 转为base64存储
      const reader = new FileReader()
      reader.onloadend = async () => {
        const base64Audio = reader.result
        try {
          await addAnnotation({
            childId: child.value?.id,
            bookId: book.value?.id,
            pageNum: currentPage.value,
            annotationType: 2, // 语音批注
            content: base64Audio // 存储base64音频数据
          })
          voiceRecordCount.value++
          ElMessage.success('🎤 语音保存成功！')
          loadAnnotations()
        } catch (error) {
          console.error('保存录音失败:', error)
          ElMessage.error('保存失败')
        }
      }
      reader.readAsDataURL(audioBlob)

      // 停止所有音轨
      stream.getTracks().forEach(track => track.stop())
    }

    mediaRecorder.start()
    isRecording.value = true
    recordingTime.value = 0

    recordingTimer = setInterval(() => {
      recordingTime.value++
      // 最长60秒
      if (recordingTime.value >= 60) {
        stopRecording()
      }
    }, 1000)

  } catch (error) {
    console.error('录音失败:', error)
    ElMessage.error('无法启动录音，请检查麦克风权限')
  }
}

// 停止录音
const stopRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
    isRecording.value = false
    if (recordingTimer) {
      clearInterval(recordingTimer)
      recordingTimer = null
    }
  }
}

// 切换录音状态
const toggleRecording = () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

// 格式化本地时间 (ISO格式)
const formatLocalDateTime = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

const saveReadingLog = async (isCompleted) => {
  if (isSaved.value) return
  isSaved.value = true

  try {
    const endTime = new Date()
    // 确保 duration 不为负数，如果 startTime 为空或计算异常则使用 readingDuration
    let duration = Math.floor((endTime - startTime.value) / 1000)
    if (!startTime.value || duration < 0) {
      duration = readingDuration.value || 0
    }
    duration = Math.max(0, duration)

    await createReadingLog({
      childId: child.value?.id,
      bookId: book.value?.id,
      startTime: formatLocalDateTime(startTime.value),
      endTime: formatLocalDateTime(endTime),
      duration: duration,
      pageTurnCount: pageTurnCount.value,
      pageStayTimes: pageStayTimes.value,
      isCompleted: isCompleted ? 1 : 0,
      // 添加互动数据
      bookmarkCount: bookmarkCount.value,
      annotationCount: annotationCount.value,
      voiceRecordCount: voiceRecordCount.value
    })
  } catch (error) {
    console.error('保存阅读记录失败:', error)
    isSaved.value = false
  }
}

const loadData = async () => {
  const bookId = route.query.bookId
  const childId = route.query.childId

  if (!bookId || !childId) {
    ElMessage.error('缺少必要参数')
    router.back()
    return
  }

  try {
    const bookRes = await getBookById(bookId)
    if (bookRes.code === 200) {
      book.value = bookRes.data
      totalPages.value = bookRes.data.pageCount || 20
    }

    const childRes = await getChildById(childId)
    if (childRes.code === 200) {
      child.value = childRes.data
    }

    // 加载互动数据
    checkBookmarkStatus()
    loadAnnotations()

    startTimer()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载失败，请重试')
  }
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  stopTimer()
  if (startTime.value) {
    saveReadingLog(false)
  }
})
</script>

<style scoped>
.reading-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #fef9f0 0%, #fff5e6 100%);
  display: flex;
  flex-direction: column;
}

.reading-header {
  background: #fff;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #2d3436;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.4);
}

.reading-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.book-title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3436;
}

.child-name {
  font-size: 13px;
  color: #a0937d;
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-mini {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #f0e6d3;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #6d4c41;
}

.reading-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

.book-display {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.page-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 0;
}

.current-page {
  width: 100%;
  max-width: 800px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.page-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.illustration-area {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(255, 255, 255, 0.3);
}

.illustration-emoji {
  font-size: 72px;
  letter-spacing: 8px;
  animation: bounce 2s infinite ease-in-out;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-6px) scale(1.03); }
}

.story-area {
  padding: 20px 28px 28px;
  background: linear-gradient(180deg, transparent 0%, rgba(255, 255, 255, 0.85) 20%);
}

.story-text {
  font-size: 18px;
  color: #3a3a3a;
  font-weight: 500;
  line-height: 2;
  text-align: justify;
  letter-spacing: 0.5px;
  margin: 0;
}

.page-indicator-inline {
  font-size: 14px;
  color: #6d4c41;
  font-weight: 600;
}

.page-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 16px 0;
}

.page-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  color: #2d3436;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.4);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 底部操作栏 */
.bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
  gap: 12px;
}

/* 互动按钮组 */
.interaction-btns {
  display: flex;
  gap: 8px;
}

.interact-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  background: #fef9f0;
  border: 2px solid #f0e6d3;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #6d4c41;
  cursor: pointer;
  transition: all 0.3s ease;
}

.interact-btn:hover {
  background: #ffeaa7;
  border-color: #fdcb6e;
  transform: translateY(-2px);
}

.interact-btn.active {
  background: #ff6b6b;
  border-color: #ff6b6b;
  color: #fff;
}

.interact-btn.recording {
  background: #e17055;
  border-color: #e17055;
  color: #fff;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.interact-btn .btn-text {
  font-size: 12px;
}

.reading-tips-inline {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #6d4c41;
  flex: 1;
  margin-right: 20px;
}

.finish-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.finish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 184, 148, 0.4);
}

.confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.confirm-content {
  background: #fff;
  padding: 32px;
  border-radius: 20px;
  text-align: center;
  max-width: 400px;
}

.confirm-content h3 {
  font-size: 20px;
  color: #2d3436;
  margin-bottom: 12px;
}

.confirm-content p {
  color: #6d4c41;
  margin-bottom: 24px;
}

.confirm-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.cancel-btn {
  padding: 12px 24px;
  background: #f0e6d3;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #6d4c41;
  cursor: pointer;
}

.confirm-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
}

/* 批注弹窗 */
.annotation-dialog,
.annotation-list-dialog {
  background: #fff;
  border-radius: 20px;
  width: 90%;
  max-width: 400px;
  max-height: 80vh;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-bottom: 2px dashed #f0e6d3;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2d3436;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  color: #6d4c41;
}

.dialog-body {
  padding: 20px;
}

.page-info {
  font-size: 13px;
  color: #a0937d;
  margin-bottom: 12px;
}

.dialog-body textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #f0e6d3;
  border-radius: 12px;
  font-size: 14px;
  resize: none;
  font-family: inherit;
}

.dialog-body textarea:focus {
  outline: none;
  border-color: #fdcb6e;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0e6d3;
}

/* 统计摘要 */
.stats-summary {
  display: flex;
  justify-content: space-around;
  padding: 16px;
  background: #fef9f0;
  border-radius: 12px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-item .stat-icon {
  font-size: 24px;
}

.stat-item .stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #2d3436;
}

.stat-item .stat-label {
  font-size: 12px;
  color: #a0937d;
}

/* 批注列表 */
.annotation-list {
  max-height: 300px;
  overflow-y: auto;
}

.annotation-item {
  padding: 12px;
  background: #fef9f0;
  border-radius: 10px;
  margin-bottom: 8px;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.item-page {
  font-size: 12px;
  font-weight: 600;
  color: #e17055;
  background: #ffeaa7;
  padding: 2px 8px;
  border-radius: 6px;
}

.item-type {
  font-size: 12px;
  color: #6d4c41;
}

.delete-btn {
  margin-left: auto;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  opacity: 0.6;
}

.delete-btn:hover {
  opacity: 1;
}

.item-content {
  margin: 0;
  font-size: 13px;
  color: #2d3436;
  line-height: 1.6;
}

.empty-list {
  text-align: center;
  padding: 40px;
  color: #a0937d;
}

.empty-list span {
  font-size: 48px;
  display: block;
  margin-bottom: 8px;
}

/* 音频播放器 */
.audio-player {
  padding: 8px 0;
}

.play-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.play-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(9, 132, 227, 0.4);
}

@media (max-width: 768px) {
  .reading-header { flex-wrap: wrap; gap: 10px; padding: 12px 16px; }
  .reading-info { order: -1; width: 100%; }
  .book-title { font-size: 16px; }
  .header-stats { flex-wrap: wrap; justify-content: center; }
  .stat-mini { padding: 8px 12px; font-size: 13px; }
  .reading-content { padding: 16px; }
  .illustration-emoji { font-size: 56px; }
  .story-text { font-size: 16px; line-height: 1.9; }
  .page-controls { flex-wrap: wrap; gap: 12px; }
  .page-btn { padding: 10px 18px; font-size: 14px; }
  .bottom-bar { flex-wrap: wrap; gap: 12px; }
  .reading-tips-inline { font-size: 13px; }
  .finish-btn { padding: 12px 24px; font-size: 15px; }
}
</style>
