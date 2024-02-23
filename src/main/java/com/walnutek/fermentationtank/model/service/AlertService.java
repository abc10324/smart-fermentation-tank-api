package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.AlertDao;
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
        if(User.Role.SUPER_ADMIN.equals(user.getRole())){
            throw new AppException(AppException.Code.E002, "此帳號無權限建立裝置");
        }
        if(StringUtils.hasText(vo.getName())) {
            var data = vo.toAlert();
            data.setLaboratoryId(laboratoryId);
            data.setStatus(BaseColumns.Status.ACTIVE);
            alertDao.insert(data);

            return data.getId();
        } else {
            throw new AppException(AppException.Code.E002, "必填欄位資料不正確");
        }
    }

    public void deleteAlert(String laboratoryId, String alertId) {
        isAlertAvailableEdit(laboratoryId, alertId);
        var data = alertDao.selectById(alertId);
        data.setStatus(BaseColumns.Status.DELETED);
        alertDao.updateById(data);
    }

    public void updateAlert(String laboratoryId, String alertId, AlertVO vo) {
        isAlertAvailableEdit(laboratoryId, alertId);
        var data = alertDao.selectById(alertId);
        data.setName(vo.getName());
        data.setType(vo.getType());
        data.setDeviceId(vo.getDeviceId());
        data.setCheckField(vo.getCheckField());
        data.setCondition(vo.getCondition());
        data.setThreshold(vo.getThreshold());
        alertDao.updateById(data);
    }

    public Page<AlertVO> search(String laboratoryId, Map<String, Object> paramMap) {
        return alertDao.search(laboratoryId, paramMap);
    }

    private void isAlertAvailableEdit(String laboratoryId, String alertId){
        checkUserIsBelongToLaboratory(laboratoryId);
        var alert = alertDao.selectByIdAndStatus(alertId, BaseColumns.Status.ACTIVE);
        if(Objects.isNull(alert)){
            throw new AppException(AppException.Code.E002, "無法更新不存在的警報");
        }
    }

}
