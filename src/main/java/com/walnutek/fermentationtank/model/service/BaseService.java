package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.auth.Auth;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.UserDao;
import com.walnutek.fermentationtank.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BaseService {
    @Autowired
    protected UserDao userDao;

    protected String getLoginUserId() {
        String userId = null;

        if(Optional.ofNullable(Auth.getAuthUser()).isPresent()) {
            userId = Auth.getAuthUser().getUserId();
        }

        return userId;
    }

    protected User getLoginUser(){
        return Optional.ofNullable(userDao.selectById(getLoginUserId()))
                .orElseThrow(() -> new AppException(AppException.Code.E004));
    }

}
