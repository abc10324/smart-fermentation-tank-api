package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.auth.Auth;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.LaboratoryDao;
import com.walnutek.fermentationtank.model.dao.UserDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BaseService {
    @Autowired
    protected UserDao userDao;

    @Autowired
    protected LaboratoryDao laboratoryDao;

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

    protected List<String> getUserLabList(){
        var user = getLoginUser();
        var userId = user.getId();
        List<String> labList = new ArrayList<>();
        switch (user.getRole()) {
            case SUPER_ADMIN -> {
                labList = laboratoryDao.selectAll().stream().map(BaseColumns::getId).toList();
            }
            case LAB_ADMIN -> {
                labList = laboratoryDao.selectByOwnerId(userId).stream().map(BaseColumns::getId).toList();
            }
            case LAB_USER -> {
                labList = userDao.getLoginUserInfo(userId).getLabList();
            }
        }
        return labList;
    }

}
