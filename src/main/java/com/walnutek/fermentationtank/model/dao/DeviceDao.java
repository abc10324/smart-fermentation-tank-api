package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Device;
import com.walnutek.fermentationtank.model.entity.Device.DeviceType;
import com.walnutek.fermentationtank.model.vo.DeviceVO;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Repository
public class DeviceDao extends BaseDao<Device> {
    @Autowired
    private MongoTemplate template;

    public Page<DeviceVO> search(String laboratoryId, List<DeviceType> type, Map<String, Object> paramMap){
        List<CriteriaBuilder> criteriaList = new ArrayList<>();
        criteriaList.add(where(Device::getLaboratoryId).is(laboratoryId));
        criteriaList.add(where(Device::getStatus).is(BaseColumns.Status.ACTIVE));
        criteriaList.add(where(Device::getType).in(type));
        var keyword = paramMap.get(Const.KEYWORD);
        if(hasText(keyword)){
            criteriaList.add(where(DeviceVO::getName).like(keyword));
        }
        var queryList = criteriaList.stream().map(CriteriaBuilder::build).toList();
        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);

        return aggregationSearch(QueryCondition.of(queryList, sort, pageable), DeviceVO.class);
    }
}
