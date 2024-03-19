package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.AlertDao;
import com.walnutek.fermentationtank.model.dao.AlertRecordDao;
import com.walnutek.fermentationtank.model.entity.*;
import com.walnutek.fermentationtank.model.vo.AlertVO;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;


@Service
@Transactional
public class AlertService extends BaseService {

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private AlertRecordDao alertRecordDao;

    @Autowired
    private LineNotifyService lineNotifyService;

    public String createAlert(String laboratoryId, AlertVO vo){
        var user = getLoginUser();
        checkUserRole(User.Role.LAB_ADMIN, user.getRole());
        checkCreateOrUpdateField(vo);
        var data = new Alert().apply(vo);
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
        var data = isAlertAvailableEdit(laboratoryId, alertId).apply(vo);
        data.setUpdateTime(LocalDateTime.now());
        data.setUpdateUser(getLoginUserId());
        alertDao.updateById(data);
    }

    public Page<AlertVO> search(String laboratoryId, Map<String, Object> paramMap) {
        return alertDao.search(laboratoryId, paramMap);
    }

    public List<Alert> findListByQuery(Map<String, Object> paramMap){
        var alertQuery = Stream.of(
                        where(hasText(paramMap.get("laboratoryId")), Alert::getLaboratoryId).is(paramMap.get("laboratoryId")),
                        where(hasText(paramMap.get("deviceId")), Alert::getDeviceId).is(paramMap.get("deviceId")),
                        where(Alert::getStatus).is(BaseColumns.Status.ACTIVE)
                        ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        return alertDao.selectList(alertQuery);
    }

    public void checkSensorUploadDataAndSendAlertRecord(Map<String, Object> paramMap, SensorRecord record){
        var alertList = findListByQuery(paramMap);
        var uploadData = record.getUploadData();
        if(Objects.nonNull(uploadData)){
            alertList.forEach(alert -> {
                var checkField = alert.getCheckField();
                if(uploadData.containsKey(checkField)){
                    if(uploadData.get(checkField) instanceof Double uploadDataValue){
                        var isIssueAlert = switch (alert.getCondition()){
                            case GREATER_THAN -> uploadDataValue > alert.getThreshold();
                            case LESS_THAN -> uploadDataValue < alert.getThreshold();
                        };

                        if(isIssueAlert){
                            var alertRecord = new AlertRecord().apply(alert, uploadDataValue);
                            alertRecordDao.insert(alertRecord);

                            lineNotifyService.sendLineNotify(alert.getLaboratoryId(), alert, alertRecord);
                        }
                    }
                }
            });
        }
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
