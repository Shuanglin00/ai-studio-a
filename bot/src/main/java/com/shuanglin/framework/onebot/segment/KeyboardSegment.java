package com.shuanglin.framework.onebot.segment;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 键盘按钮消息段
 */
@Getter
public class KeyboardSegment extends MessageSegment {

    private List<KeyboardRow> rows = new ArrayList<>();

    public KeyboardSegment() {
        this.type = "keyboard";
    }

    public KeyboardSegment addRow(KeyboardRow row) {
        this.rows.add(row);
        return this;
    }

    @Override
    public void validate() {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Keyboard must have at least one row");
        }
    }

    /**
     * 键盘按钮行
     */
    @Data
    public static class KeyboardRow {
        private List<KeyboardButton> buttons = new ArrayList<>();

        public KeyboardRow addButton(KeyboardButton button) {
            this.buttons.add(button);
            return this;
        }
    }

    /**
     * 键盘按钮
     */
    @Data
    public static class KeyboardButton {
        private String id;
        private RenderData renderData;
        private Action action;

        public static KeyboardButtonBuilder builder() {
            return new KeyboardButtonBuilder();
        }

        public static class KeyboardButtonBuilder {
            private final KeyboardButton button = new KeyboardButton();

            public KeyboardButtonBuilder id(String id) {
                button.id = id;
                return this;
            }

            public KeyboardButtonBuilder label(String label) {
                if (button.renderData == null) {
                    button.renderData = new RenderData();
                }
                button.renderData.label = label;
                return this;
            }

            public KeyboardButtonBuilder visitedLabel(String visitedLabel) {
                if (button.renderData == null) {
                    button.renderData = new RenderData();
                }
                button.renderData.visitedLabel = visitedLabel;
                return this;
            }

            public KeyboardButtonBuilder style(int style) {
                if (button.renderData == null) {
                    button.renderData = new RenderData();
                }
                button.renderData.style = style;
                return this;
            }

            public KeyboardButtonBuilder actionType(int actionType) {
                if (button.action == null) {
                    button.action = new Action();
                }
                button.action.type = actionType;
                return this;
            }

            public KeyboardButtonBuilder actionData(String data) {
                if (button.action == null) {
                    button.action = new Action();
                }
                button.action.data = data;
                return this;
            }

            public KeyboardButton build() {
                return button;
            }
        }

        @Data
        public static class RenderData {
            private String label;
            private String visitedLabel;
            private Integer style;
        }

        @Data
        public static class Action {
            private Integer type;
            private Permission permission;
            private String unsupportTips;
            private String data;
            private Boolean reply;
            private Boolean enter;

            @Data
            public static class Permission {
                private Integer type;
                private List<String> specifyRoleIds = new ArrayList<>();
                private List<String> specifyUserIds = new ArrayList<>();
            }
        }
    }
}
