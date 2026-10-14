package com.example.chat.utils;

import com.example.chat.enums.Emotion;
import com.example.chat.enums.Emoji;


public class EmojiUtil {

    private static final int MIN = 1;
    private static final int MAX = 10;

    private EmojiUtil() {}

    public static String pick(Emotion emotion, int intensity) {
        return emotion == null
                ? Emoji.UNKNOWN.getKey()
                : build(emotion.getEmoji(), intensity);
    }

    private static String build(Emoji emoji, int intensity) {
        if (emoji == null) {
            return Emoji.UNKNOWN.getKey();
        }
        int i = Math.max(MIN, Math.min(MAX, intensity));
        int m = (i - 1) * emoji.getCount() / 10 + 1;
        if (emoji.getKey().equals(Emoji.UNKNOWN.getKey())) {
            return emoji.getKey();
        }
        return emoji.getKey() + "_"  + m;
    }
}
