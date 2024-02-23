package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.dao.AlertDao;
import com.walnutek.fermentationtank.model.dao.AlertRecordDao;
import com.walnutek.fermentationtank.model.dao.DeviceDao;
import com.walnutek.fermentationtank.model.entity.*;
import com.walnutek.fermentationtank.model.vo.DashboardDataVO;
import com.walnutek.fermentationtank.model.vo.AlertRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;
import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
public class AlertRecordService {

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private AlertRecordDao alertRecordDao;

    @Autowired
    private DeviceDao deviceDao;

    public List<DashboardDataVO> listAllGroupByLaboratoryId(
            List<String> userLabList,
            Map<String,String> userLabMap,
            Map<String, Object> paramMap
    ) {
        var alertQuery = Stream.of(
                        where(Alert::getLaboratoryId).in(userLabList),
                        where(Alert::getStatus).is(BaseColumns.Status.ACTIVE)
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        var alertList = alertDao.selectList(alertQuery);
        var alertIdList = alertList.stream().map(BaseColumns::getId).toList();
        var deviceIdList = alertList.stream().map(Alert::getDeviceId).toList();
        Map<String, List<Alert>> map = alertList.stream()
                .collect(groupingBy(Alert::getLaboratoryId));
        var alertRecordQuery = Stream.of(
                        where(AlertRecord::getAlertId).in(alertIdList),
                        where(hasText(paramMap.get("state")), AlertRecord::getState).is(paramMap.get("state"))
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        var alertRecordList = alertRecordDao.selectList(alertRecordQuery);
        var alertRecordMap = alertRecordList.stream().collect(Collectors.toMap(AlertRecord::getAlertId, Function.identity()));
        var deviceQuery = Stream.of(
                        where(Device::getId).in(deviceIdList),
                        where(Alert::getStatus).is(BaseColumns.Status.ACTIVE)
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        var deviceList = deviceDao.selectList(deviceQuery);
        var deviceMap = deviceList.stream().collect(Collectors.toMap(Device::getId, Device::getName));
        var resulList = new ArrayList<DashboardDataVO>();
        for (String laboratoryId : map.keySet()) {
            if(userLabMap.containsKey(laboratoryId)){
                var vo = new DashboardDataVO();
                var laboratoryName = userLabMap.get(laboratoryId);
                vo.laboratory = laboratoryName;
                vo.laboratoryId = laboratoryId;
                var dataList = new ArrayList<AlertRecordVO>();
                map.get(laboratoryId).forEach( alert -> {
                    var alertRecord = alertRecordMap.get(alert.getId());
                    var device = deviceMap.get(alert.getDeviceId());
                    var alertRecordVO = AlertRecordVO.of(alertRecord, alert, device, laboratoryName);
                    dataList.add(alertRecordVO);
                });
                vo.total = dataList.size();
                vo.data = dataList;
                resulList.add(vo);
            }
        }
        return resulList;
    }
}
