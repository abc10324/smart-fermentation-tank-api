package com.walnutek.fermentationtank.model.dao;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.vo.LaboratoryVO;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Stream;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;
import static com.walnutek.fermentationtank.model.service.Utils.hasArray;
import static com.walnutek.fermentationtank.model.service.Utils.ObjectToList;

@Repository
public class LaboratoryDao extends BaseDao<Laboratory> {
    public List<Laboratory> selectByOwnerId(String ownerId) {
        return selectList(
                List.of(
                        where(Laboratory::getStatus).is(BaseColumns.Status.ACTIVE).build(),
                        where(Laboratory::getOwnerId).is(ownerId).build()
                )
        );
    }

    public Page<LaboratoryVO> search(Map<String, Object> paramMap){
        return aggregationSearch(getQueryCondition(paramMap), LaboratoryVO.class);
    }

    private QueryCondition getQueryCondition(Map<String,Object> paramMap) {
        var criteriaList = Stream.of(
                        where(hasArray(paramMap.get("labList")), Laboratory::getId).in(ObjectToList(paramMap.get("labList"))),
                        where(hasText(paramMap.get("status")), Laboratory::getStatus).is(paramMap.get("status")),
                        where(hasText(paramMap.get(Const.KEYWORD)), Laboratory::getName).like(paramMap.get(Const.KEYWORD))
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();

        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);
        var condition = QueryCondition.of(criteriaList, sort, pageable);
        condition.setIsBeforeLookupCondition(true);
        return condition;
    }
}
