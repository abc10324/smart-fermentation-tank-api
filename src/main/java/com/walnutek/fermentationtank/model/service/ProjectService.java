package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.ProjectDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Project;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.DashboardDataVO;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.ProjectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
public class ProjectService extends BaseService {

    @Autowired
    private ProjectDao projectDao;

    public String createProject(String laboratoryId, ProjectVO vo) {
        var user = getLoginUser();
        checkUserRole(User.Role.SUPER_ADMIN, user.getRole());
        checkCreateOrUpdateField(vo);
        var data = vo.toProject(new Project());
        data.setLaboratoryId(laboratoryId);
        data.setStatus(BaseColumns.Status.ACTIVE);
        projectDao.insert(data);

        return data.getId();
    }

    public void deleteProject(String laboratoryId, String projectId){
        var data = isProjectAvailableEdit(laboratoryId, projectId);
        data.setStatus(BaseColumns.Status.DELETED);
        projectDao.updateById(data);
    }

    public void updateProject(String laboratoryId, String projectId, ProjectVO vo) {
        var data = isProjectAvailableEdit(laboratoryId, projectId);
        checkCreateOrUpdateField(vo);
        projectDao.updateById(vo.toProject(data));
    }

    public Page<ProjectVO> search(String laboratoryId, Map<String, Object> paramMap) {
        return projectDao.search(laboratoryId,  paramMap);
    }

    public List<DashboardDataVO> listAllGroupByLaboratoryId(
            List<String> userLabList,
            Map<String,String> userLabMap,
            Map<String,String> deviceLabMap
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
                    var device = Optional.ofNullable(deviceLabMap.get(project.getDeviceId())).orElse("");
                    return ProjectVO.of(project, laboratoryName, device);
                }).toList();
                vo.total = projectVOList.size();
                vo.data = projectVOList;
                resulList.add(vo);
            }
        }
        return resulList;
    }

    private Project isProjectAvailableEdit(String laboratoryId, String projectId) {
        checkUserIsBelongToLaboratory(laboratoryId);
        var project = projectDao.selectByIdAndStatus(projectId, BaseColumns.Status.ACTIVE);
        if(Objects.isNull(project)){
            throw new AppException(AppException.Code.E002, "無法更新不存在的專案");
        }else {
            return project;
        }
    }

    private void checkCreateOrUpdateField(ProjectVO vo){
        if(!StringUtils.hasText(vo.getName())
                || !StringUtils.hasText(vo.getDeviceId())
                    || Objects.isNull(vo.getStartTime())
                        || Objects.isNull(vo.getEndTime())
        ) throw new AppException(AppException.Code.E002, "必填欄位資料不正確");
    }
}
