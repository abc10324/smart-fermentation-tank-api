package com.walnutek.fermentationtank.model.dao;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
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
                        where(hasArray(paramMap.get("labList")), Laboratory::getId).in(paramMap.get("labList")),
                        where(hasText(paramMap.get("status")), Laboratory::getStatus).in(paramMap.get("status")),
                        where(hasText(paramMap.get("keyword")), Laboratory::getName).like(paramMap.get("keyword"))
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();

        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);

        return QueryCondition.of(criteriaList, sort, pageable);
    }
}
