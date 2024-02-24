package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.DeviceDao;
import com.walnutek.fermentationtank.model.dao.LaboratoryDao;
import com.walnutek.fermentationtank.model.dao.SensorDao;
import com.walnutek.fermentationtank.model.dao.SensorRecordDao;
import com.walnutek.fermentationtank.model.entity.*;
import com.walnutek.fermentationtank.model.entity.BaseColumns.Status;
import com.walnutek.fermentationtank.model.entity.Device.DeviceType;
import com.walnutek.fermentationtank.model.vo.DashboardDataVO;
import com.walnutek.fermentationtank.model.vo.DeviceVO;
import com.walnutek.fermentationtank.model.vo.DeviceVO.ConnectionStatus;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.ProjectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
public class DeviceService extends BaseService {

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private LaboratoryDao laboratoryDao;

    @Autowired
    private SensorDao sensorDao;

    @Autowired
    private SensorRecordDao sensorRecordDao;

    public String createDevice(DeviceVO vo) {
        var user = getLoginUser();
        checkUserRole(User.Role.SUPER_ADMIN, user.getRole());
        checkCreateOrUpdateField(vo);
        var data = vo.toDevice(new Device());
        data.setStatus(Status.ACTIVE);
        deviceDao.insert(data);

        return data.getId();
    }

    public void deleteDevice(String laboratoryId, String deviceId){
        var data = isDeviceAvailableEdit(laboratoryId, deviceId);
        data.setStatus(BaseColumns.Status.DELETED);
        deviceDao.updateById(data);
    }

    public void updateDevice(String laboratoryId, String deviceId, DeviceVO vo) {
        checkCreateOrUpdateField(vo);
        var data = isDeviceAvailableEdit(laboratoryId, deviceId);
        deviceDao.updateById(data);
    }

    public ConnectionStatus checkDeviceConnectionStatus(String laboratoryId, String deviceId){
        // 不需要查label 只要有其中一個sensor有收到紀錄即可
        var sensorQuery = List.of(
                where(Sensor::getLaboratoryId).is(laboratoryId).build(),
                where(Sensor::getDeviceId).is(deviceId).build()
        );
        var sensor = Optional.ofNullable(sensorDao.selectOne(sensorQuery))
                .orElseThrow(() -> new AppException(AppException.Code.E004));
        var checkPointTime = LocalDateTime.now().minusMinutes(30);
        var sensorRecordQuery = List.of(
                where(SensorRecord::getSensorId).is(sensor.getId()).build(),
                where(Sensor::getCreateTime).gte(checkPointTime).build()
        );
        return Objects.nonNull(sensorRecordDao.selectOne(sensorRecordQuery))
                ?ConnectionStatus.NORMAL:ConnectionStatus.EXCEPTION;
    }

    public Page<DeviceVO> search(String laboratoryId, DeviceType type, Map<String, Object> paramMap) {
        var resultPage = deviceDao.search(laboratoryId, getDeviceTypeList(type), paramMap);
        var lab = laboratoryDao.selectById(laboratoryId);
        resultPage.getRecords().forEach(device -> {
            device.setLaboratory(lab.getName());
            device.setConnectionStatus(checkDeviceConnectionStatus(laboratoryId, device.getId()));
        });
        return resultPage;
    }

    public List<DeviceVO> list(String laboratoryId, DeviceType type) {
        var laboratory = Optional.ofNullable(laboratoryDao.selectById(laboratoryId))
                .orElseThrow(() -> new AppException(AppException.Code.E004));
        var labName = laboratory.getName();
        var query = List.of(
                where(Device::getLaboratoryId).is(laboratoryId).build(),
                where(Device::getStatus).is(Status.ACTIVE).build(),
                where(Device::getType).in(getDeviceTypeList(type)).build()
        );
        return deviceDao.selectList(query).stream().map(device -> {
            var connectionStatus = checkDeviceConnectionStatus(laboratoryId, device.getId());
            return DeviceVO.of(device, labName, connectionStatus);
        }).toList();
    }

    public List<DashboardDataVO> listAllGroupByLaboratoryId(
            List<String> userLabList, Map<String,String> userLabMap){
        var deviceQuery = List.of(
                where(Device::getLaboratoryId).in(userLabList).build(),
                where(Device::getStatus).is(Status.ACTIVE).build()
        );
        Map<String, List<Device>> map = deviceDao.selectList(deviceQuery).stream()
                .collect(groupingBy(Device::getLaboratoryId));
        var resulList = new ArrayList<DashboardDataVO>();
        for (String laboratoryId : map.keySet()) {
            if(userLabMap.containsKey(laboratoryId)){
                var vo = new DashboardDataVO();
                var laboratoryName = userLabMap.get(laboratoryId);
                vo.laboratory = laboratoryName;
                vo.laboratoryId = laboratoryId;
                var deviceVOList = map.get(laboratoryId).stream().map(device -> {
                    var connectionStatus = checkDeviceConnectionStatus(laboratoryId, device.getId());
                    return DeviceVO.of(device, laboratoryName, connectionStatus);
                }).toList();
                vo.total = deviceVOList.size();
                vo.data = deviceVOList;
                resulList.add(vo);
            }
        }
        return resulList;
    }

    public List<Device> listByQuery(List<Criteria> criteriaList){
        return deviceDao.selectList(criteriaList);
    }

    private Device isDeviceAvailableEdit(String laboratoryId, String deviceId) {
        checkUserIsBelongToLaboratory(laboratoryId);
        var device = deviceDao.selectByIdAndStatus(deviceId, BaseColumns.Status.ACTIVE);
        if(Objects.isNull(device)){
            throw new AppException(AppException.Code.E002, "無法更新不存在的裝置");
        }else {
            return device;
        }
    }

    private void checkCreateOrUpdateField(DeviceVO vo){
        if(!StringUtils.hasText(vo.getName())
                || !StringUtils.hasText(vo.getLaboratoryId())
        ) throw new AppException(AppException.Code.E002, "必填欄位資料不正確");
    }

    private List<DeviceType> getDeviceTypeList(DeviceType type){
        switch (type){
            case FERMENTER -> {
                return List.of(type);
            }
            default -> {
                return Arrays.stream(DeviceType.values()).toList();
            }
        }
    }
}
