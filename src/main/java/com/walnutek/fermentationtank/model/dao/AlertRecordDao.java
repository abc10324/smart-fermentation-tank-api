package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.entity.Alert;
import com.walnutek.fermentationtank.model.entity.AlertRecord;
import com.walnutek.fermentationtank.model.entity.Device;
import com.walnutek.fermentationtank.model.vo.AlertRecordVO;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Repository
public class AlertRecordDao extends BaseDao<AlertRecord> {

    public Page<AlertRecordVO> search(String laboratoryId, Map<String, Object> paramMap){
        List<CriteriaBuilder> beforeLookupCriteriaList = new ArrayList<>();
        beforeLookupCriteriaList.add(where(AlertRecord::getLaboratoryId).is(laboratoryId));
        var alertState = paramMap.get("alertState");
        if(hasText(alertState)){
            beforeLookupCriteriaList.add(where(AlertRecord::getState).like(alertState));
        }
        List<CriteriaBuilder> afterLookupCriteriaList = new ArrayList<>();
        var keyword = paramMap.get(Const.KEYWORD);
        if(hasText(keyword)){
            afterLookupCriteriaList.add(
                    where(Alert::getName).like(keyword)
                    .or(where(Alert::getType).like(keyword))
                    .or(where(Alert::getCheckField).like(keyword))
                    .or(where(Device::getName).like(keyword))
            );
        }
        var beforeLookupCondition = beforeLookupCriteriaList.stream().map(CriteriaBuilder::build).toList();
        var afterLookupCondition = afterLookupCriteriaList.stream().map(CriteriaBuilder::build).toList();
        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);

        return aggregationSearch(
                QueryCondition.of(beforeLookupCondition, sort, pageable),
                QueryCondition.of(afterLookupCondition),
                AlertRecord.class,
                AlertRecordVO.class
        );
    }
}
