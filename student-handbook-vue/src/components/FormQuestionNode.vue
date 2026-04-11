<template>
  <div class="form-question-node" :class="{ 'is-nested': level > 0 }">
    <!-- Question Header -->
    <div class="node-header">
      <span class="badge blue">問題 {{ displayNum }}</span>
      <span class="badge light-grey">{{ getTypeName(question.type) }} {{ getTypeSuffix(question) }}</span>
      <span class="badge pink" v-if="question.required">必答</span>
    </div>

    <!-- Question Title -->
    <div class="node-title-row">
      <span class="label">問題標題：</span>
      <span class="title-text">{{ question.title }}</span>
    </div>

    <!-- Question Body (Options or Fill Blanks) -->
    <div class="node-body">
      <!-- Options for Single/Multiple Choice -->
      <template v-if="['1', '2'].includes(String(question.type))">
        <div class="option-item" v-for="(opt, oIdx) in question.options" :key="oIdx">
          <div class="option-header">
            <span class="option-text">{{ String.fromCharCode(65 + oIdx) }}. {{ opt }}</span>
            <span class="jump-badge" v-if="getJumpTarget(oIdx)">
              <span class="link-icon">🔗</span> 跳轉至{{ getJumpDisplayText(getJumpTarget(oIdx)) }}
            </span>
          </div>

          <!-- Nested Children -->
          <div class="nested-children-wrapper" v-if="getChildNodes(oIdx).length > 0">
            <FormQuestionNode 
              v-for="child in getChildNodes(oIdx)" 
              :key="child.node.id"
              :question="child.node"
              :all-nodes="allNodes"
              :level="level + 1"
              :displayNum="child.displayNum"
            />
          </div>
        </div>
      </template>

      <!-- Fill in the blank -->
      <template v-else-if="String(question.type) === '3'">
        <div class="fill-blank-content" v-html="renderFillBlanks(question)"></div>
      </template>

      <!-- Upload -->
      <template v-else-if="String(question.type) === '4'">
        <div class="upload-note">{{ question.uploadNote || '此處由用戶端上傳...' }}</div>
      </template>

      <!-- Other JSON Content Fallback? -->
      <template v-else-if="question.content && !question.fillBlanks">
         <div class="fallback-content">{{ question.content }}</div>
      </template>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FormQuestionNode',
  props: {
    question: {
      type: Object,
      required: true
    },
    allNodes: {
      type: Array,
      required: true
    },
    level: {
      type: Number,
      default: 0
    },
    displayNum: {
      type: String,
      default: ''
    }
  },
  methods: {
    getTypeName(type) {
      const typeStr = String(type);
      return { '1': '單選', '2': '多選', '3': '填空', '4': '附件上傳' }[typeStr] || typeStr;
    },
    getTypeSuffix(q) {
      if (String(q.type) === '2' && (q.minOptions || q.maxOptions)) {
        return `(可選 ${q.minOptions || 0}～${q.maxOptions || '多'} 項)`;
      }
      return '';
    },
    getJumpTarget(optIdx) {
      if (!this.question.logicRuleList) return null;
      const rule = this.question.logicRuleList.find(r => r.optionIndex === optIdx);
      return rule ? rule.jumpTarget : null;
    },
    getJumpDisplayText(target) {
      if (target === 'next') return '下一題';
      if (target === 'end') return '結束';

      // Find target node to get its generated displayNum
      const targetNode = this.allNodes.find(n => n.node.id === Number(target) || n.node.id === target);
      if (targetNode) {
        return `問題 ${targetNode.displayNum}`;
      }
      return `問題 ${target}`;
    },
    getChildNodes(optIdx) {
      // Find all nodes in allNodes whose parentId is this question's id and parentOptIdx is optIdx
      return this.allNodes.filter(n => n.parentId === this.question.id && n.parentOptIdx === optIdx);
    },
    renderFillBlanks(q) {
      if (!q.content) return '';
      // Replace placeholders like {{fillblank-1}} with underline
      return q.content.replace(/\{\{fillblank-\d+\}\}/g, '<span style="display:inline-block; border-bottom:1px solid #333; width:150px; margin: 0 5px;"></span>');
    }
  }
}
</script>

<style scoped>
.form-question-node {
  margin-bottom: 20px;
  background: white;
}

.form-question-node.is-nested {
  margin-top: 15px;
  margin-bottom: 5px;
  border: 1px dashed #dccdce;
  border-radius: 8px;
  padding: 15px;
  background: #fdfdfd;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: bold;
}

.badge.blue {
  background: #e6f7ff;
  color: #1890ff;
}

.badge.light-grey {
  background: #f4f4f5;
  color: #909399;
  font-weight: normal;
  border: 1px solid #e9e9eb;
}

.badge.pink {
  background: #fff0f6;
  color: #eb2f96;
  border: 1px solid #ffadd2;
}

.node-title-row {
  margin-bottom: 15px;
  font-size: 15px;
}

.node-title-row .label {
  color: #909399;
}

.node-title-row .title-text {
  color: #303133;
  font-weight: 600;
}

.node-body {
  padding-left: 15px;
}

.option-item {
  margin-bottom: 15px;
}

.option-item:last-child {
  margin-bottom: 0;
}

.option-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 8px;
}

.option-text {
  font-size: 14px;
  color: #606266;
}

.jump-badge {
  font-size: 13px;
  color: #e6a23c;
  background: #fdf6ec;
  border: 1px solid #faecd8;
  padding: 2px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.link-icon {
  font-size: 12px;
}

.nested-children-wrapper {
  margin-left: 20px;
  margin-top: 10px;
  border-left: 2px solid #e4e7ed;
  padding-left: 15px;
}

.fill-blank-content {
  font-size: 15px;
  line-height: 2;
  color: #333;
}

.upload-note {
  font-size: 14px;
  color: #909399;
  padding: 10px;
  background: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  text-align: center;
}
</style>
