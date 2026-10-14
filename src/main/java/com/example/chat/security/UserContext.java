package com.example.chat.security;

import com.example.chat.exception.UnauthorizedException;

/**
 * 当前请求的登录用户（由 AuthInterceptor 在 preHandle 时写入）。
 * ChatService 的会话 key 是 String，取值统一走 {@link #userIdString()}。
 */
public final class UserContext {

    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(Long userId) { HOLDER.set(userId); }

    public static Long get() { return HOLDER.get(); }

    public static Long require() {
        Long id = HOLDER.get();
        if (id == null) throw new UnauthorizedException("未登录");
        return id;
    }

    /** ChatService 以 String 为会话 key，JWT subject 即数据库 id 的字符串形式 */
    public static String userIdString() {
        return String.valueOf(require());
    }

    public static void clear() { HOLDER.remove(); }
}
