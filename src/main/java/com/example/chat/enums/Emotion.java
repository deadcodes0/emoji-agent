package com.example.chat.enums;


import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 每句话从这些枚举中选一个，映射到对应的 emoji 素材。
 */
@Getter
public enum Emotion {

    ANGRY_1("不满", Emoji.ANGRY_1),
    ANGRY_2("七窍生烟", Emoji.ANGRY_2),
    ANGRY_3("火冒三丈", Emoji.ANGRY_3),
    ANGRY_4("破口大骂", Emoji.ANGRY_4),
    PEEP("暗中观察", Emoji.PEEP),
    ENTREAT("哀求", Emoji.ENTREAT),
    SPARKLED("两眼放光", Emoji.SPARKLED),
    HEARTBEAT("心动", Emoji.HEARTBEAT),
    TENSION_1("汗流浃背（形容紧张）", Emoji.TENSION_1),
    TENSION_2("冷汗直流", Emoji.TENSION_2),
    SCREAM("尖叫（害怕）", Emoji.SCREAM),
    SALUTE("敬礼", Emoji.SALUTE),
    PRAY("双手合十，不断叩头（乞求）", Emoji.PRAY),
    ADORE("崇拜（星星眼）", Emoji.STAR_EYES),
    INTIMATE("贴贴", Emoji.INTIMATE),
    PLAYFULNESS_1("俏皮(眨一只眼)", Emoji.PLAYFULNESS_1),
    PLAYFULNESS_2("俏皮撩人", Emoji.PLAYFULNESS_2),
    LAUGH("笑喷", Emoji.LAUGH),
    LOSE("失落", Emoji.LOSE),
    YAWN("哈欠", Emoji.YAWN),
    TIRED("疲倦", Emoji.TIRED),
    CALM_1("心如止水", Emoji.CALM_1),
    CALM_2("淡定", Emoji.CALM_2),
    CELEBRATE("庆祝", Emoji.CELEBRATE),
    LISTENER("认真倾听", Emoji.LISTENER),
    HUGGING("敞开怀抱", Emoji.HUGGING),
    AMAZED("咦?(惊讶)", Emoji.AMAZED),
    PUZZLED("一头雾水", Emoji.PUZZLED),
    SNEER("冷笑", Emoji.SNEER),
    CONTEMPT("轻蔑一笑", Emoji.CONTEMPT),
    CONTENTMENT("心满意足", Emoji.CONTENTMENT),
    COLLAPSE("大脑宕机", Emoji.COLLAPSE),
    TEARING("当场裂开", Emoji.TEARING),
    DELICIOUS("馋嘴", Emoji.DELICIOUS),
    DROOL("垂涎欲滴", Emoji.DROOL),
    CRYING("泪流成河", Emoji.CRYING),
    CURSING("脏话连篇", Emoji.CURSING),
    SCAN("侦探脸(审视)", Emoji.SCAN),
    CONSIDER("笨拙思索", Emoji.CONSIDER),
    PICK_NOSE("粗犷卖萌(抠鼻)", Emoji.PICK_NOSE),
    PAIN("仰天长啸(极度悲伤)", Emoji.PAIN),
    SAD("唉声叹气", Emoji.SAD),
    DISLIKE("嫌弃", Emoji.DISLIKE),
    EMBARRASSED("尬笑", Emoji.EMBARRASSED),
    GUILTY("(龇牙咧嘴地笑)心虚", Emoji.GUILTY),
    SHAME_SMILE("害羞微笑", Emoji.SHAME_SMILE),
    PRETEND_CALM("故作镇定", Emoji.PRETEND_CALM),
    SPIRIT_FREE("灵魂出窍", Emoji.SPIRIT_FREE),
    CLAP("尴尬捧场(鼓掌)", Emoji.CLAP),
    THUMB("呆萌点赞", Emoji.THUMB),
    NO_PROBLEM("轻松拿捏", Emoji.NO_PROBLEM),
    BIU_BIU("滑稽比枪(搞怪)", Emoji.BIU_BIU),
    ENVY("羡慕", Emoji.ENVY),
    SNICKER("坏笑", Emoji.SNICKER),
    EVIL_1("恶魔大笑", Emoji.DEVIL_LAUGH),
    EVIL_2("恶魔愤怒", Emoji.DEVIL_ANGER),
    BORED("黑眼圈（无聊）", Emoji.DARK_CIRCLE),
    FLAUNT_WEALTH("炫富(喷名牌香水)", Emoji.FLAUNT_WEALTH),
    BIND_UP("包扎（心理/身体受伤）", Emoji.BIND_UP),
    APPRECIATE("双手递上莲花(感激不尽)", Emoji.APPRECIATE),
    SMACK_FACE("啪啪打脸", Emoji.SMACK_FACE),
    GRIEVANCE("内疚", Emoji.GRIEVANCE),
    SLEEPY("黑眼圈(困倦)", Emoji.DARK_CIRCLE),
    HAPPY("高兴", Emoji.HAPPY),
    THINKING("思考", Emoji.THINKING),
    DEAFNESS("充耳不闻（双手捂着耳朵，故意不听）", Emoji.DEAFNESS),
    JEALOUSY("嫉妒", Emoji.JEALOUSY),
    ELATED("心花怒放", Emoji.ELATED),
    LIE("说谎", Emoji.LIE),
    LOVE("爱意", Emoji.LOVE),
    MOVED("感动", Emoji.MOVED),
    ARROGANCE("傲慢", Emoji.ARROGANCE),
    SELF_MOCKERY("自嘲(小丑脸)", Emoji.SELF_MOCKERY),
    SHOW_OFF("耍帅（墨镜脸）", Emoji.SHOW_OFF),
    HELPLESSNESS("无助", Emoji.HELPLESSNESS),
    REGRET("后悔（痛哭流涕）", Emoji.REGRET),
    SPEECHLESS("无语", Emoji.SPEECHLESS),
    REJECTION("拒绝（双手交叉）", Emoji.REJECTION),
    SIGH("无奈（叹气）", Emoji.SIGH),
    TALKING("讲话", Emoji.TALKING),
    SLACK_OFF("摸鱼", Emoji.SLACK_OFF),
    SLEEPING("睡着了", Emoji.SLEEPING),
    SPOOF_1("略略略（得瑟）", Emoji.SPOOF_1),
    SPOOF_2("装疯卖傻（吐舌头）", Emoji.SPOOF_2),
    STRESS("压力大到爆炸（头爆炸了）", Emoji.STRESS),
    ENCOURAGE("鼓励（伸出大拇指）", Emoji.ENCOURAGE),
    CURIOSITY("好奇（头顶有问号）", Emoji.CURIOSITY),
    EXPECT("期待（搓手）", Emoji.EXPECT),
    SURPRISE_1("惊呆了(流鼻涕)", Emoji.SURPRISE_1),
    SURPRISE_2("目瞪口呆", Emoji.SURPRISE_2),
    BEYOND_BELIEF("难以置信（瞪大眼睛）", Emoji.BEYOND_BELIEF),
    COMFORT("送花(安慰)", Emoji.COMFORT),
    EAT_MELON("吃瓜", Emoji.EAT_MELON),
    DIZZY("晕", Emoji.DIZZY),
    DOUBT("怀疑", Emoji.DOUBT),
    ADMIRE("钦佩，欣赏", Emoji.ADMIRE),
    CARE("关心", Emoji.HUGGING),
    EXHALE("松了口气（如释重负）", Emoji.EXHALE),
    ADVISE("提建议", Emoji.TALKING),
    UNKNOWN("未知", Emoji.UNKNOWN);

    private final String label;
    private final Emoji emoji;

    Emotion(String label, Emoji emoji) {
        this.label = label;
        this.emoji = emoji;
    }

    public static String allLabels() {
        return Arrays.stream(values())
                .map(Emotion::getLabel)
                .collect(Collectors.joining(","));
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
    private static final Map<String, Emotion> BY_LABEL =
            Arrays.stream(values())
                    .collect(Collectors.toMap(
                            Emotion::getLabel,
                            Function.identity(),
                            (a, b) -> a));   // 万一有重复 label，保留先出现的

    public static Emotion fromLabel(String label) {
        if (label == null) {
            return UNKNOWN;
        }
        return BY_LABEL.getOrDefault(label, UNKNOWN);
    }

}
