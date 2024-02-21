package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.auth.AuthUser;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import com.walnutek.fermentationtank.model.dao.LaboratoryDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.DashboardDataVO;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;

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

    public List<DashboardDataVO> listAllGroupByLaboratoryId(
            List<String> userLabList,
            Map<String,String> userLabMap
    ){
        var userQuery = List.of(
                where(User::getLabList).in(userLabList).build(),
                where(User::getStatus).is(BaseColumns.Status.ACTIVE).build()
        );
        var list = userDao.selectList(userQuery);
        var resulList = new ArrayList<DashboardDataVO>();
        userLabList.forEach(laboratoryId ->{
            var vo = new DashboardDataVO();
            var laboratoryName = userLabMap.get(laboratoryId);
            vo.laboratory = laboratoryName;
            vo.laboratoryId = laboratoryId;
            var dataList = new ArrayList<UserVO>();
            list.forEach(user -> {
                if(user.getLabList().contains(laboratoryId)){
                    var userVO = new UserVO();
                    userVO.setRole(user.getRole());
                    userVO.setAccount(user.getAccount());
                    userVO.setName(user.getName());
                    userVO.setEmail(user.getEmail());
                    userVO.setLabList(user.getLabList());
                    userVO.setLabNameList(getLabNameList(userLabMap, user.getLabList()));
                    dataList.add(userVO);
                }
            });
            vo.total = dataList.size();
            vo.data = dataList;
            resulList.add(vo);
        });
        return resulList;
    }

    private List<String> getLabNameList(Map<String,String> userLabMap, List<String> userLabList){
        return userLabList.stream().map(userLabMap::get).toList();
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
