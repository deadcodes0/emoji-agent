package com.example.chat.enums;


import lombok.Getter;

/**
 * 每句话从这些枚举中选一个，映射到对应的 emoji 素材。
 */
@Getter
public enum Emotion {

    ANGER("愤怒", Emoji.ANGER), //
    ANTICIPATION("期待", Emoji.ANTICIPATION), //
    ANXIETY("焦虑", Emoji.ANXIETY), //
    AWE("敬畏", Emoji.AWE), //
    BELONGING("归属", Emoji.BELONGING), //
    BOREDOM("无聊", Emoji.BOREDOM), //
    CALM("平静", Emoji.CALM), //
    COMFORT("安抚", Emoji.COMFORT), //
    CONFUSION("困惑", Emoji.CONFUSION), //
    CONTEMPT("轻蔑", Emoji.CONTEMPT), //
    CONTENTMENT("满足", Emoji.CONTENTMENT),
    CURIOSITY("好奇", Emoji.CURIOSITY), //
    DESPAIR("绝望", Emoji.DESPAIR), //
    DISAPPOINTMENT("失望", Emoji.DISAPPOINTMENT), //
    DISGUST("厌恶", Emoji.DISGUST), //
    DISSOCIATION("解离感", Emoji.DISSOCIATION),
    EMBARRASSMENT("尴尬", Emoji.EMBARRASSMENT), //
    EMPATHY("共情", Emoji.SYMPATHY), //
    EMPTINESS("空虚", Emoji.EMPTINESS), //
    ENCOURAGE("鼓励", Emoji.ENCOURAGE), //
    ENVY("羡慕", Emoji.ENVY), //
    FATIGUE("疲惫", Emoji.FATIGUE), //
    FEAR("恐惧", Emoji.FEAR), //
    FRUSTRATION("挫败", Emoji.FRUSTRATION), //
    GENTLE("温柔", Emoji.LOVE), //
    GRATITUDE("感激", Emoji.GRATITUDE), //
    GRIEVANCE("委屈", Emoji.GRIEVANCE), //
    GUILT("内疚", Emoji.GUILT), //
    HELPLESSNESS("无助", Emoji.HELPLESSNESS),
    HOSTILITY("敌意", Emoji.EVIL), //
    INDECISION("犹豫", Emoji.INDECISION), //
    IRRITABILITY("烦躁", Emoji.IRRITABILITY), //
    JEALOUSY("嫉妒", Emoji.JEALOUSY), //
    JOY("喜悦", Emoji.JOY), //
    LONELINESS("孤独", Emoji.LONELINESS),
    LONGING("思念", Emoji.LONGING),
    LOVE("爱意", Emoji.LOVE), //
    MOVED("感动", Emoji.MOVED), //
    NUMBNESS("麻木", Emoji.NUMBNESS), //
    PLAYFULNESS("俏皮", Emoji.PLAYFULNESS),   // 仅低风险可用
    PRIDE("自豪", Emoji.PRIDE), //
    REGRET("后悔", Emoji.REGRET), //
    REJECTION("拒绝", Emoji.REJECTION), //
    RELIEF("释然", Emoji.RELIEF), //
    SADNESS("悲伤", Emoji.CRYING), //
    SECURITY("安心", Emoji.COMFORT), //
    SERIOUS("严肃", Emoji.SERIOUS), //
    SHAME("害羞", Emoji.SHAME), //
    STRESS("压力", Emoji.STRESS), //
    SUPPORTIVE("支持", Emoji.BELONGING),
    SURPRISE("惊讶", Emoji.SURPRISE), //
    SYMPATHY("同情", Emoji.SYMPATHY), //
    BEGGING("恳求", Emoji.PRAYER),   //
    PRAYER("祈祷", Emoji.PRAYER),    //
    FLAUNT("炫耀", Emoji.FLAUNT), //
    SPEECHLESS("无语", Emoji.SPEECHLESS), //
    CRAVING("馋或垂涎", Emoji.CRAVING), //
    NERVOUSNESS("紧张", Emoji.NERVOUSNESS), //
    CRYING("大哭", Emoji.CRYING), //
    CURSING("咒骂", Emoji.CURSING), //
    EVIL("邪恶", Emoji.EVIL), //
    CUTE("卖萌", Emoji.CUTE), //
    SHOW_OFF("耍帅", Emoji.SHOW_OFF), //
    SLACK_OFF("摸鱼", Emoji.SLACK_OFF), //
    CORRUPTED("崩溃", Emoji.CORRUPTED), //
    SELF_MOCKING("自嘲", Emoji.SELF_MOCKING),//
    SPOOF("搞怪", Emoji.SPOOF),//
    LIE("说谎", Emoji.LIE),//
    ROLL("滚", Emoji.ROLL),//
    CELEBRATE("庆祝", Emoji.CELEBRATE), //
    UNKNOWN("未知", Emoji.UNKNOWN);

    private final String label;
    private final Emoji emoji;

    Emotion(String label, Emoji emoji) {
        this.label = label;
        this.emoji = emoji;
    }



    /**
     * 根据枚举名安全解析，找不到时返回 {@link #UNKNOWN}。
     */
    public static Emotion fromName(String name) {
        if (name == null || name.isBlank()) {
            return UNKNOWN;
        }
        for (Emotion key : values()) {
            if (key.name().equalsIgnoreCase(name)) {
                return key;
            }
        }
        return UNKNOWN;
    }
}
