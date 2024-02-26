package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.entity.*;
import com.walnutek.fermentationtank.model.vo.AlertRecordVO;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.ProjectVO;
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
public class ProjectDao extends BaseDao<Project> {

    public Page<ProjectVO> search(String laboratoryId, Map<String, Object> paramMap) {
        List<CriteriaBuilder> beforeLookupCriteriaList = new ArrayList<>();
        beforeLookupCriteriaList.add(where(Project::getLaboratoryId).is(laboratoryId));
        beforeLookupCriteriaList.add(where(Project::getStatus).is(BaseColumns.Status.ACTIVE));
        List<CriteriaBuilder> afterLookupCriteriaList = new ArrayList<>();
        var keyword = paramMap.get(Const.KEYWORD);
        if(hasText(keyword)){
            afterLookupCriteriaList.add(
                    where(Project::getName).like(keyword)
                            .or(where(LOOKUP_COLLECTION_DEVICE + ".name").like(keyword))
            );
        }
        var beforeLookupCondition = beforeLookupCriteriaList.stream().map(CriteriaBuilder::build).toList();
        var afterLookupCondition = afterLookupCriteriaList.stream().map(CriteriaBuilder::build).toList();
        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);

        return aggregationSearch(
                QueryCondition.of(beforeLookupCondition, sort, pageable),
                QueryCondition.of(afterLookupCondition),
                Project.class,
                ProjectVO.class
        );
    }

}
