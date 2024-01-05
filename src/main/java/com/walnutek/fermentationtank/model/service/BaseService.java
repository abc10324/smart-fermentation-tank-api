package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.auth.Auth;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BaseService {

    protected String getLoginUserId() {
        String userId = null;

        if(Optional.ofNullable(Auth.getAuthUser()).isPresent()) {
            userId = Auth.getAuthUser().getUserId();
        }

        return userId;
    }

}
