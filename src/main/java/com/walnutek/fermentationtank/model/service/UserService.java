package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.auth.AuthUser;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import com.walnutek.fermentationtank.model.dao.LaboratoryDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.entity.User.Role;
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
        var userId = user.getId();
        var data = new User().apply(vo);
        Role targetRole;
        switch (user.getRole()) {
            case SUPER_ADMIN -> {
                data.setLabList(List.of());
                targetRole = Role.LAB_ADMIN;
            }
            case LAB_ADMIN -> {
                data.setAdminId(userId);
                targetRole = User.Role.LAB_USER;
            }
            default -> throw new AppException(Code.E002, "此帳號無權限建立使用者");
        };
        checkCreateOrUpdateField(vo, true, data);

        data.setRole(targetRole);
        data.setPassword(encryptPassword(data.getPassword()));
        data.setStatus(BaseColumns.Status.ACTIVE);
        userDao.insert(data);
        return data.getId();
    }

    public void updateUser(UserVO vo) {
        updateUser(getLoginUserId(), vo);
	}

    public void updateUser(String userId, UserVO vo) {
        var data = isUserAvailableEdit(userId);
        checkCreateOrUpdateField(vo, false, data);
        var user = data.apply(vo);
        if(vo.getPassword() != null){
            var password = encryptPassword(vo.getPassword());
            user.setPassword(password);
        }
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateUser(getLoginUserId());
        userDao.updateById(user);
    }

    public void updateUserStatus(String userId, User.Status isActive) {
        var user = isUserAvailableEdit(userId);
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

    public UserVO getUserProfile(String userId) {
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

    private String encryptPassword(String password){
        return Optional.ofNullable(password)
                .filter(StringUtils::hasText)
                .map(cipherService::encrypt)
                .orElse(password);
    }

    private User isUserAvailableEdit(String userId){
        var user = userDao.selectByIdAndStatus(userId, BaseColumns.Status.ACTIVE);
        if(Objects.isNull(user)){
            throw new AppException(AppException.Code.E002, "無法更新不存在的使用者");
        }else {
            return user;
        }
    }

    private void checkCreateOrUpdateField(UserVO vo, Boolean isCreate, User originalUser){
        var checkResult = new ArrayList<String>();

        // 新增或更新都做的基本檢查 帳號 角色 名字 email
        if(!StringUtils.hasText(vo.getAccount())) checkResult.add("使用者帳號");
        if(Objects.isNull(vo.getRole())) checkResult.add("使用者角色");
        if(!StringUtils.hasText(vo.getName())) checkResult.add("使用者名稱");
        if(!StringUtils.hasText(vo.getEmail())) checkResult.add("使用者email");
        // 新增或更新都做 只有一般使用者要檢查
        if(Role.LAB_USER.equals(vo.getRole())){
            if(Objects.isNull(vo.getLabList()) || vo.getLabList().isEmpty())
                checkResult.add("所屬實驗室列表");
        }
        // 只有新增 檢查密碼
        if(isCreate && !StringUtils.hasText(vo.getPassword())) checkResult.add("密碼");
        // 只有新增或更新使用者帳號要檢查 email 是否重複
        if(isCreate || !Objects.equals(originalUser.getAccount(), vo.getAccount())){
            if(isAccountExist(vo.getAccount())) checkResult.add("欲更改的帳號已存在");
        }
        if(!checkResult.isEmpty()){
            StringBuilder errorMsg  = new StringBuilder();
            errorMsg.append("錯誤訊息：");
            var lastIndex = checkResult.size()-1;
            for (int i = 0; i < checkResult.size(); i++) {
                var field = checkResult.get(i);
                if ("欲更改的帳號已存在".equals(field)) {
                    errorMsg.append(field);
                } else {
                    errorMsg.append(field).append("欄位資料不正確");
                }
                if(i < lastIndex){
                    errorMsg.append(", ");
                }
            }
            throw new AppException(AppException.Code.E002, errorMsg.toString());
        }
    }
}
