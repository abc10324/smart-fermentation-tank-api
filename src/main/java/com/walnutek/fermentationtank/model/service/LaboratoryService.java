package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.LaboratoryVO;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

import static com.walnutek.fermentationtank.model.entity.User.Role.SUPER_ADMIN;

@Service
@Transactional
public class LaboratoryService extends BaseService {

    public String createLaboratory(LaboratoryVO vo) {
        var user = getLoginUser();
        checkUserRole(User.Role.LAB_ADMIN, user.getRole());
        checkCreateOrUpdateField(vo);
        var data = new Laboratory().apply(vo);
        data.setOwnerId(user.getId());
        data.setStatus(BaseColumns.Status.ACTIVE);
        laboratoryDao.insert(data);

        return data.getId();
    }

    public void deleteLaboratory(String id){
        var data = isLabAvailableEdit(id);
        data.setStatus(BaseColumns.Status.DELETED);
        laboratoryDao.updateById(data);
    }

    public void updateLaboratory(String id, LaboratoryVO vo) {
        checkCreateOrUpdateField(vo);
        var data = isLabAvailableEdit(id).apply(vo);
        data.setUpdateTime(LocalDateTime.now());
        data.setUpdateUser(getLoginUserId());
        laboratoryDao.updateById(data);
    }

    public Page<LaboratoryVO> search(Map<String, Object> paramMap) {
        Page<LaboratoryVO> result = Page.emptyPage();
        var user = getLoginUser();
        var userId = user.getId();
        List<String> labList = new ArrayList<>();
        switch (user.getRole()) {
            case LAB_ADMIN -> labList = laboratoryDao.selectByOwnerId(userId).stream().map(BaseColumns::getId).toList();
            case LAB_USER -> labList = userDao.userValidCheckAndGetUserInfo(userId).getLabList();
        }
        if(!SUPER_ADMIN.equals(user.getRole()) && labList.isEmpty()){
            return result;
        }
        paramMap.put("labList",labList);
        paramMap.put("status", BaseColumns.Status.ACTIVE);
        result = laboratoryDao.search(paramMap);
        return result;
    }

    public List<Laboratory> listByQuery(List<Criteria> criteriaList){
        return laboratoryDao.selectList(criteriaList);
    }


    private Laboratory isLabAvailableEdit(String id) {
        var lab = laboratoryDao.selectByIdAndStatus(id, BaseColumns.Status.ACTIVE);
        checkUserIsLaboratoryOwner(lab.getOwnerId());
        if(Objects.isNull(lab)){
            throw new AppException(AppException.Code.E002, "無法更新不存在的實驗室");
        }else {
            return lab;
        }
    }

    private void checkCreateOrUpdateField(LaboratoryVO vo){
        if(!StringUtils.hasText(vo.getName())) throw new AppException(AppException.Code.E002, "必填欄位資料不正確");
    }

}
