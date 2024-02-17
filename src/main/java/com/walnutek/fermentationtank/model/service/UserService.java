package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.auth.AuthUser;
import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import com.walnutek.fermentationtank.model.dao.LaboratoryDao;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasArray;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Service
@Transactional
public class UserService extends BaseService {

    @Autowired
    private LaboratoryDao laboratoryDao;

    @Autowired
    private CipherService cipherService;

    public String createUser(UserVO vo) {
        var user = getLoginUser();
        var targetRole = switch (user.getRole()) {
            case SUPER_ADMIN -> User.Role.LAB_ADMIN;
            case LAB_ADMIN -> User.Role.LAB_USER;
            default -> throw new AppException(Code.E002, "此帳號無權限建立使用者");
        };

        if(StringUtils.hasText(vo.getAccount()) &&
            StringUtils.hasText(vo.getPassword()) &&
            !isAccountExist(vo.getAccount())) {

            var data = vo.toUser();
            data.setRole(targetRole);
            data.setPassword(encryptPassword(data.getPassword()));

            if(User.Role.LAB_ADMIN.equals(data.getRole())) {
                data.setLabList(List.of());
            } else if (User.Role.LAB_USER.equals(data.getRole())) {
                data.setAdminId(getLoginUserId());
            }

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
    		var data = userDao.userValidCheckAndGetUserInfo(id);

            if(!Objects.equals(data.getAccount(), vo.getAccount()) && isAccountExist(vo.getAccount())) {
                throw new AppException(Code.E002, "欲更改的帳號已存在");
            }

            data.setAccount(vo.getAccount());
			data.setName(vo.getName());
			data.setEmail(vo.getEmail());
			data.setLabList(vo.getLabList());

            if(StringUtils.hasText(vo.getPassword())) {
                data.setPassword(encryptPassword(vo.getPassword()));
            }

    		userDao.updateById(data);
    	} else {
    		throw new AppException(Code.E002, "無法更新不存在的使用者");
    	}
    }

    public void updateUserStatus(String userId, User.Status isActive) {
        var user = userDao.userValidCheckAndGetUserInfo(userId);
        user.setStatus(isActive);
        userDao.updateById(user);
    }

    public void updateUserPassword(String oldPassword, String newPassword) {
        var user = getLoginUser();
        if(!user.getPassword().equals(encryptPassword(oldPassword))) {
            throw new AppException(Code.E006);
        }
        user.setPassword(encryptPassword(newPassword));
        userDao.updateById(user);
    }

    public Page<UserVO> search(Map<String, Object> paramMap) {
        return userDao.search(paramMap);
    }

    public AuthUser UserLoginCheck(String account, String password) {
        var user = Optional.ofNullable(userDao.getUserByAccountAndPassword(account, encryptPassword(password)))
                           .orElseThrow(() -> new AppException(Code.E001));
        addLoginCount(user);

        return AuthUser.of(user);
    }

    public AuthUser getLoginUserInfo() {
        var userId = getLoginUserId();
        var user = userDao.userValidCheckAndGetUserInfo(userId);
        return AuthUser.of(user);
    }

    public UserVO getUserProfile() {
        var userId = getLoginUserId();
        var user = userDao.userValidCheckAndGetUserInfo(userId);
        user.setPassword(null);
        return UserVO.of(user);
	}

    public boolean isAccountExist(String account) {
        return userDao.isAccountExist(account);
	}

    public List<Laboratory> getAvailableLabList(String userId) {
        var user = userDao.userValidCheckAndGetUserInfo(userId);
        List<Laboratory> resultList = new ArrayList<>();
        switch (user.getRole()) {
            case SUPER_ADMIN -> laboratoryDao.selectAll().forEach(resultList::add);
            case LAB_ADMIN -> laboratoryDao.selectByOwnerId(userId).forEach(resultList::add);
            case LAB_USER -> {
                if(StringUtils.hasText(user.getAdminId())) {
                    laboratoryDao.selectByOwnerId(user.getAdminId()).forEach(resultList::add);
                }
            }
        }

        return resultList;
    }

    public List<Laboratory> getOwnLabList() {
        List<Laboratory> resultList = new ArrayList<>();
        var user = getLoginUser();
        switch (user.getRole()) {
            case SUPER_ADMIN -> laboratoryDao.selectAll().forEach(resultList::add);
            case LAB_ADMIN -> laboratoryDao.selectByOwnerId(getLoginUserId()).forEach(resultList::add);
            case LAB_USER -> Optional.ofNullable(user.getLabList())
                                .filter(Objects::nonNull)
                                .map(laboratoryDao::selectByIds)
                                .ifPresent(resultList::addAll);
        }
        return resultList;
    }

    public Integer countUserNum(Map<String, Object> paramMap){
        System.out.println(paramMap);
        var query = Stream.of(
                where(hasText(paramMap.get("adminId")), User::getAdminId).is(paramMap.get("adminId")),
                where(hasText(paramMap.get("role")), User::getRole).is(paramMap.get("role")),
                where(hasText(paramMap.get("name")), User::getName).like(paramMap.get("name")),
                where(hasText(paramMap.get("email")), User::getEmail).like(paramMap.get("email")),
                where(hasArray(paramMap.get("labList")), User::getLabList).in(paramMap.get("labList")),
                where(hasText(paramMap.get("status")), User::getStatus).is(paramMap.get("status"))
        ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        query.forEach(data -> {
            System.out.println(data.getCriteriaObject());
        });
//        System.out.println(query);
        return Math.toIntExact(userDao.count(query));
    }

    private void addLoginCount(User user) {
        Integer currentCount = user.getLoginCount();

        user.setLoginCount(++currentCount);
        user.setLastLoginTime(LocalDateTime.now());

        userDao.updateById(user);
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
}
