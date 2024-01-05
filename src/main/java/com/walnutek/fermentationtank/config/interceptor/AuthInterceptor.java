package com.walnutek.fermentationtank.config.interceptor;

import com.walnutek.fermentationtank.config.auth.Auth;
import com.walnutek.fermentationtank.config.auth.JwtService;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
    private JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.name().equals(request.getMethod())) {
            return true;
        }
        String token = Optional.ofNullable(request.getHeader("Authorization"))
    						   .orElse("")
    						   .replaceAll("Bearer ", "");
        
        if (!StringUtils.hasText(token)) {
            throw new AppException(Code.E001, "使用者未登入");
        } else if (jwtService.isTokenExpire(token)) {
            throw new AppException(Code.E001, "使用者登入逾時");
        }
        Auth.setAuthUser(jwtService.reconstructAuthUserFromToken(token));
        response.setHeader("Authorization", String.format("Bearer %s", jwtService.refreshToken(token)));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        Auth.removeAuthUser();
    }
    
}
