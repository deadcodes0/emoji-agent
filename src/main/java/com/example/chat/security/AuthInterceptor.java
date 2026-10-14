package com.example.chat.security;

import com.example.chat.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 只读 Authorization header（Bearer token），无 query 兜底。
 * 鉴权放拦截器而不是 Controller 内部：preHandle 阶段响应尚未提交，
 * SSE 接口（返回 Flux）抛出的 401 也能正常返回 JSON。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        // CORS 预检放行，否则前端拿不到响应头
        if (HttpMethod.OPTIONS.matches(req.getMethod())) return true;

        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER)) {
            throw new UnauthorizedException("缺少登录凭证");
        }
        String token = header.substring(BEARER.length()).trim();
        try {
            UserContext.set(jwtService.parseUserId(token));
        } catch (Exception e) {
            throw new UnauthorizedException("登录凭证无效或已过期");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp,
                                Object handler, Exception ex) {
        // Tomcat 线程复用，必须清理
        UserContext.clear();
    }
}
