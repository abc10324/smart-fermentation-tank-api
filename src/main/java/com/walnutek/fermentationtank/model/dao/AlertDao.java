package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.entity.Alert;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Device;
import com.walnutek.fermentationtank.model.vo.AlertVO;
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
public class AlertDao extends BaseDao<Alert> {

    @Autowired
    private MongoTemplate template;

    public Page<AlertVO> search(String laboratoryId, Map<String, Object> paramMap){
        List<CriteriaBuilder> criteriaList = new ArrayList<>();
        criteriaList.add(where(Device::getLaboratoryId).is(laboratoryId));
        criteriaList.add(where(Device::getStatus).is(BaseColumns.Status.ACTIVE));
        var keyword = paramMap.get("keyword");
        if(hasText(keyword)){
            criteriaList.add(where(DeviceVO::getName).like(keyword));
        }
        var queryList = criteriaList.stream().map(CriteriaBuilder::build).toList();
        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);

        return aggregationSearch(QueryCondition.of(queryList, sort, pageable), Alert.class, AlertVO.class);
    }
}
