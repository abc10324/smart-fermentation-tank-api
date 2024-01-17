package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.auth.Auth;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.LaboratoryDao;
import com.walnutek.fermentationtank.model.dao.UserDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.LaboratoryVO;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Transactional
public class LaboratoryService extends BaseService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private LaboratoryDao laboratoryDao;

    public Page<LaboratoryVO> search(Map<String, Object> paramMap) {
        var user = getLoginUser();
        var userId = getLoginUserId();
        List<String> labList = new ArrayList<>();
        var result = new Page().EmptyPage();
        switch (user.getRole()) {
            case LAB_ADMIN -> {
                labList = laboratoryDao.selectByOwnerId(userId).stream().map(BaseColumns::getId).toList();
            }
            case LAB_USER -> {
                labList = userDao.getLoginUserInfo(userId).getLabList();
                if(labList.isEmpty()){
                    return result;
                }
            }
        }
        paramMap.put("labList",labList);
        result = laboratoryDao.search(paramMap);
        return result;
    }

    public String createLaboratory(LaboratoryVO vo) {
        var user = getLoginUser();
        if(!User.Role.LAB_ADMIN.equals(user.getRole())){
            throw new AppException(AppException.Code.E002, "此帳號無權限建立實驗室");
        }
        if(StringUtils.hasText(vo.getName())) {
            var data = vo.toLaboratory();
            data.setOwnerId(user.getId());
            data.setName(vo.getName());
            data.setNote(vo.getNote());
            data.setStatus(BaseColumns.Status.ACTIVE);
            laboratoryDao.insert(data);

            return data.getId();
        } else {
            throw new AppException(AppException.Code.E002, "必填欄位資料不正確");
        }
    }

    public void updateLaboratory(String id, LaboratoryVO vo) {
        isLabAvailableEdit(id);
        var data = laboratoryDao.selectById(id);
        data.setName(vo.getName());
        data.setNote(vo.getNote());
        data.setStatus(vo.getStatus());
        laboratoryDao.updateById(data);
    }

    private void isLabAvailableEdit(String id) {
        var userId = getLoginUserId();
        var lab = laboratoryDao.selectByIdAndStatus(id, BaseColumns.Status.ACTIVE);
        if(Objects.isNull(lab)){
            throw new AppException(AppException.Code.E002, "無法更新不存在的實驗室");
        }
        if(!userId.equals(lab.getOwnerId())){
            throw new AppException(AppException.Code.E002, "非實驗室管理者無法編輯");
        }
    }

}