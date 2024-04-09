package com.walnutek.fermentationtank.model.service;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        var password = encryptPassword(vo.getPassword());
        user.setPassword(password);
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
    	var labOwnerMap = laboratoryDao.selectList(List.of(where(Laboratory::getId).in(userLabList).build()))
							    		.stream()
							    		.collect(groupingBy(Laboratory::getOwnerId, mapping(Laboratory::getId, toList())));
    	
        var userQuery = List.of(
                where(User::getLabList).in(userLabList)
                .or(where(User::getId).in(labOwnerMap.keySet())).build(),
                where(User::getStatus).is(BaseColumns.Status.ACTIVE).build()
        );
        var list = userDao.selectList(userQuery);
        list.forEach(user -> {
        	if(labOwnerMap.containsKey(user.getId())) {
        		user.setLabList(labOwnerMap.get(user.getId()));
        	}
        });
        
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

    private void checkCreateOrUpdateField(UserVO vo, Boolean isCreate, User data){
        if(!StringUtils.hasText(vo.getAccount())
                || !StringUtils.hasText(vo.getPassword())
                || Objects.isNull(vo.getRole())
                || !StringUtils.hasText(vo.getName())
                || !StringUtils.hasText(vo.getEmail())
                || Objects.isNull(vo.getLabList())
                || vo.getLabList().isEmpty()
        ) throw new AppException(AppException.Code.E002, "必填欄位資料不正確");

        if(isCreate || !Objects.equals(data.getAccount(), vo.getAccount())){
            if(isAccountExist(vo.getAccount())) throw new AppException(Code.E002, "欲更改的帳號已存在");
        }
    }
}
