package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.model.dao.ProjectDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Project;
import com.walnutek.fermentationtank.model.vo.DashboardDataVO;
import com.walnutek.fermentationtank.model.vo.ProjectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
public class ProjectService extends BaseService {

    @Autowired
    private ProjectDao projectDao;

    public List<DashboardDataVO> listAllGroupByLaboratoryId(
            List<String> userLabList,
            Map<String,String> userLabMap,
            Map<String,String> fermenterLabMap
    ) {
        var projectQuery = List.of(
                where(Project::getLaboratoryId).in(userLabList).build(),
                where(Project::getStatus).is(BaseColumns.Status.ACTIVE).build()
        );
        var list = projectDao.selectList(projectQuery);
        Map<String, List<Project>> map = list.stream()
                .collect(groupingBy(Project::getLaboratoryId));
        var resulList = new ArrayList<DashboardDataVO>();
        for (String laboratoryId : map.keySet()) {
            if(userLabMap.containsKey(laboratoryId)){
                var vo = new DashboardDataVO();
                var laboratoryName = userLabMap.get(laboratoryId);
                vo.laboratory = laboratoryName;
                vo.laboratoryId = laboratoryId;
                var projectVOList = map.get(laboratoryId).stream().map(project -> {
                    var fermenter = Optional.ofNullable(fermenterLabMap.get(project.getFermenterId())).orElse("");
                    return ProjectVO.of(project, laboratoryName, fermenter);
                }).toList();
                vo.total = projectVOList.size();
                vo.data = projectVOList;
                resulList.add(vo);
            }
        }
        return resulList;
    }
}
