package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.entity.LineNotify;
import com.walnutek.fermentationtank.model.vo.LineNotifyVO;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.walnutek.fermentationtank.config.Const.LOOKUP_COLLECTION_USER;
import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasArray;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Repository
public class LineNotifyDao extends BaseDao<LineNotify> {

    public Page<LineNotifyVO> search(Map<String, Object> paramMap) {
        List<CriteriaBuilder> beforeLookupCriteriaList = new ArrayList<>();
        if(hasText(paramMap.get("laboratoryId"))){
            beforeLookupCriteriaList.add(where(LineNotify::getLaboratoryId).is(paramMap.get("laboratoryId")));
        }
        if(hasArray(paramMap.get("userLabList"))){
            beforeLookupCriteriaList.add(where(LineNotify::getLaboratoryId).in(paramMap.get("userLabList")));
        }

        List<CriteriaBuilder> afterLookupCriteriaList = new ArrayList<>();
        var keyword = paramMap.get(Const.KEYWORD);
        if(hasText(keyword)){
            afterLookupCriteriaList.add(
                    (where(LOOKUP_COLLECTION_USER + ".name").like(keyword))
            );
        }

        var beforeLookupCondition = beforeLookupCriteriaList.stream().map(CriteriaBuilder::build).toList();
        var afterLookupCondition = afterLookupCriteriaList.stream().map(CriteriaBuilder::build).toList();
        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);

        return aggregationSearch(
                QueryCondition.of(beforeLookupCondition, sort, pageable),
                QueryCondition.of(afterLookupCondition),
                LineNotify.class,
                LineNotifyVO.class
        );
    }

    public List<LineNotifyVO> searchAsList(Map<String, Object> paramMap) {
        List<CriteriaBuilder> lookupCriteriaList = new ArrayList<>();
        if(hasText(paramMap.get("laboratoryId"))){
            lookupCriteriaList.add(where(LineNotify::getLaboratoryId).is(paramMap.get("laboratoryId")));
        }
        if(hasArray(paramMap.get("userLabList"))){
            lookupCriteriaList.add(where(LineNotify::getLaboratoryId).in(paramMap.get("userLabList")));
        }
        var queryCondition = lookupCriteriaList.stream().map(CriteriaBuilder::build).toList();

        return aggregationSelectList(
                QueryCondition.of(queryCondition),
                LineNotify.class,
                LineNotifyVO.class
        );
    }
}
