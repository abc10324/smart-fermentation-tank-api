package com.walnutek.fermentationtank.config;

import com.walnutek.fermentationtank.config.auth.Auth;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class UserAuditing implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(Auth.getAuthUser().getUserId());
    }
}
