package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.auth.AuthUser;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import com.walnutek.fermentationtank.model.dao.UserDao;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.UserVO;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class UserService extends BaseService {

    @Autowired
    private UserDao userDao;
    
    @Autowired
    private CipherService cipherService;

    public Page<UserVO> search(Map<String, Object> paramMap) {
        return userDao.search(paramMap);
    }




    public String createUser(UserVO vo) {
        if(StringUtils.hasText(vo.getAccount()) &&
            StringUtils.hasText(vo.getPassword()) &&
            !isAccountExist(vo.getAccount())) {

            var data = vo.toUser();
            data.setRole(vo.getRole());
            data.setPassword(encryptPassword(data.getPassword()));

            userDao.insert(data);

			return data.getId();
        } else {
            throw new AppException(Code.E002, "必填欄位資料不正確");
        }
    }


    public void updateUser(UserVO vo) {
		updateUser(getLoginUserId(), vo);
	}
    
    public void updateUser(String id, UserVO vo) {
    	if(isUserExist(id)) {
    		var data = userDao.selectById(id);
			data.setName(vo.getName());
			data.setEmail(vo.getEmail());
			data.setLabList(vo.getLabIdList().stream().map(ObjectId::new).toList());
    		
    		userDao.updateById(data);
    	} else {
    		throw new AppException(Code.E002, "無法更新不存在的使用者");
    	}
    }
    
    private boolean isUserExist(String id) {
    	return userDao.existById(id);
    }

    private String encryptPassword(String password){
        return Optional.ofNullable(password)
                .filter(StringUtils::hasText)
                .map(cipherService::encrypt)
                .orElse(password);
    }

    public AuthUser getLoginUser(String account, String password) {
        var user = Optional.ofNullable(userDao.getLoginUser(account, encryptPassword(password)))
                           .orElseThrow(() -> new AppException(Code.E001));
        userValidCheck(user.getUserId());
        addLoginCount(user.getUserId());

        return user;
    }

    public AuthUser getLoginUserInfo() {
        userValidCheck(getLoginUserId());
        return userDao.getLoginUserInfo(getLoginUserId());
    }

    private void addLoginCount(String userId) {
        var user = userDao.selectById(userId);
        Integer currentCount = user.getLoginCount();

        user.setLoginCount(++currentCount);
        user.setLastLoginTime(LocalDateTime.now());

        userDao.updateById(user);
    }

    private void userValidCheck(String userId) {
        var user = userDao.selectById(userId);

        if(User.Status.DELETED.equals(user.getStatus())) {
            throw new AppException(Code.E001, "帳號已刪除");
        }
    }

    public UserVO getUserProfile() {
		return Optional.ofNullable(userDao.selectById(getLoginUserId(), UserVO.class))
					   .map(vo -> {
						   vo.setPassword(null);
						   return vo;
					   })
					   .orElseThrow(() -> new AppException(Code.E004));
	}

    public void updateUserStatus(String userId, User.Status isActive) {
        var user = Optional.ofNullable(userDao.selectById(userId))
                           .orElseThrow(() -> new AppException(Code.E004));
        user.setStatus(isActive);

        userDao.updateById(user);
    }
    
    public boolean isAccountExist(String account) {
		return userDao.isAccountExist(account);
	}

	public void updateUserPassword(String oldPassword, String newPassword) {
		var user = Optional.ofNullable(userDao.selectById(getLoginUserId()))
						   .orElseThrow(() -> new AppException(Code.E004));
		
		if(!user.getPassword().equals(encryptPassword(oldPassword))) {
			throw new AppException(Code.E006);
		}
		
		user.setPassword(encryptPassword(newPassword));
		userDao.updateById(user);
	}

}
