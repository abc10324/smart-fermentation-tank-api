package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.AlertDao;
import com.walnutek.fermentationtank.model.entity.Alert;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.AlertVO;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;


@Service
@Transactional
public class AlertService extends BaseService {

    @Autowired
    private AlertDao alertDao;

    public String createAlert(String laboratoryId, AlertVO vo){
        var user = getLoginUser();
        checkUserRole(User.Role.SUPER_ADMIN, user.getRole());
        checkCreateOrUpdateField(vo);
        var data = vo.toAlert(new Alert());
        data.setLaboratoryId(laboratoryId);
        data.setStatus(BaseColumns.Status.ACTIVE);
        alertDao.insert(data);

        return data.getId();
    }

    public void deleteAlert(String laboratoryId, String alertId) {
        var data = isAlertAvailableEdit(laboratoryId, alertId);
        data.setStatus(BaseColumns.Status.DELETED);
        alertDao.updateById(data);
    }

    public void updateAlert(String laboratoryId, String alertId, AlertVO vo) {
        checkCreateOrUpdateField(vo);
        var data = isAlertAvailableEdit(laboratoryId, alertId);
        alertDao.updateById(vo.toAlert(data));
    }

    public Page<AlertVO> search(String laboratoryId, Map<String, Object> paramMap) {
        return alertDao.search(laboratoryId, paramMap);
    }

    private Alert isAlertAvailableEdit(String laboratoryId, String alertId){
        checkUserIsBelongToLaboratory(laboratoryId);
        var alert = alertDao.selectByIdAndStatus(alertId, BaseColumns.Status.ACTIVE);
        if(Objects.isNull(alert)){
            throw new AppException(AppException.Code.E002, "無法更新不存在的警報");
        }else {
            return alert;
        }
    }

    private void checkCreateOrUpdateField(AlertVO vo){
        if(!StringUtils.hasText(vo.getName())
                || Objects.isNull(vo.getType())
                || !StringUtils.hasText(vo.getDeviceId())
                || !StringUtils.hasText(vo.getCheckField())
                || Objects.isNull(vo.getThreshold())
        ) throw new AppException(AppException.Code.E002, "必填欄位資料不正確");
    }

}
