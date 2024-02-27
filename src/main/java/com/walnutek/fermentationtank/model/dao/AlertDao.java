package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.Const;
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

import static com.walnutek.fermentationtank.config.Const.LOOKUP_COLLECTION_DEVICE;
import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Repository
public class AlertDao extends BaseDao<Alert> {

    @Autowired
    private MongoTemplate template;

    public Page<AlertVO> search(String laboratoryId, Map<String, Object> paramMap){
        List<CriteriaBuilder> beforeLookupCriteriaList = new ArrayList<>();
        beforeLookupCriteriaList.add(where(Alert::getLaboratoryId).is(laboratoryId));
        beforeLookupCriteriaList.add(where(Alert::getStatus).is(BaseColumns.Status.ACTIVE));
        List<CriteriaBuilder> afterLookupCriteriaList = new ArrayList<>();
        var keyword = paramMap.get(Const.KEYWORD);
        if(hasText(keyword)){
            afterLookupCriteriaList.add(
                    where(Alert::getName).like(keyword)
                    .or(where(Alert::getType).like(keyword))
                    .or(where(Alert::getCheckField).like(keyword))
                    .or(where(LOOKUP_COLLECTION_DEVICE+".name").like(keyword))
            );
        }
        var beforeLookupCondition = beforeLookupCriteriaList.stream().map(CriteriaBuilder::build).toList();
        var afterLookupCondition = afterLookupCriteriaList.stream().map(CriteriaBuilder::build).toList();
        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);

        return aggregationSearch(
                QueryCondition.of(beforeLookupCondition, sort, pageable),
                QueryCondition.of(afterLookupCondition),
                Alert.class,
                AlertVO.class
        );
    }
}
