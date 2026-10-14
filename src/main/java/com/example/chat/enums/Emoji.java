package com.example.chat.enums;

public enum Emoji {

    ANGER("anger", 4), //
    ANTICIPATION("anticipation", 4), //
    ANXIETY("anxiety", 4), //
    AWE("awe", 3),
    BELONGING("belonging", 1),
    BLINK("blink", 2), //
    BOREDOM("boredom", 3),
    CALM("calm", 3),
    CONFUSION("confusion", 2),
    CONTEMPT("contempt", 3),
    CONTENTMENT("contentment", 1),
    CURIOSITY("curiosity", 1),
    DESPAIR("despair", 1),
    DISAPPOINTMENT("disappointment", 1),
    DISGUST("disgust", 1),
    DISSOCIATION("dissociation", 1),
    EMBARRASSMENT("embarrassment", 3),
    EMPTINESS("emptiness", 1),
    ENVY("envy", 1),
    FATIGUE("fatigue", 1),
    FEAR("fear", 3),
    FRUSTRATION("frustration", 1),
    GRATITUDE("gratitude", 3),
    GRIEVANCE("grievance", 1),
    GUILT("guilt", 1),
    HELPLESSNESS("helplessness", 1),
    ENCOURAGE("encourage", 4),
    HOSTILITY("hostility", 1),
    INDECISION("indecision", 1),
    IRRITABILITY("irritability", 3),
    JEALOUSY("jealousy", 1),
    JOY("smile", 7),
    LONELINESS("loneliness", 1),
    LONGING("longing", 1),
    LOVE("love", 1),
    MOVED("moved", 1),
    NUMBNESS("numbness", 1),
    PRIDE("pride", 1),
    REGRET("regret", 1),
    REJECTION("rejection", 2),
    RELIEF("relief", 1),
    COMFORT("comfort", 2),
    SHAME("shame", 1),
    STRESS("stress", 1),
    SURPRISE("surprise", 4),
    SYMPATHY("sympathy", 2),
    UNKNOWN("unknown", 1),
    SERIOUS("serious", 1),
    PRAYER("prayer", 1),
    FLAUNT("flaunt", 1),
    SPEECHLESS("speechless", 3),
    CRAVING("craving", 2),
    NERVOUSNESS("anxiety", 2),
    CURSING("cursing", 2),
    PLAYFULNESS("playfulness", 5),
    CRYING("crying", 1),
    EVIL("evil", 3),
    CUTE("cute", 4),
    SLACK_OFF("slack_off", 1),
    CORRUPTED("corrupted", 2),
    SHOW_OFF("show_off", 2),
    SELF_MOCKING("self_mocking", 1),
    SPOOF("spoof", 4),
    ROLL("roll", 1),//
    LIE("lie", 1),
    CELEBRATE("celebrate", 1); //

    private final String key;    // 小写 key，也是 emoji 前缀
    private final int count;     // emoji 档位数 n

    Emoji(String key, int count) {
        if (count < 1) {
            throw new IllegalArgumentException(name() + " 的 count 必须 >= 1");
        }
        this.key = key;
        this.count = count;
    }

    public String getKey() { return key; }
    public int getCount() { return count; }
}
