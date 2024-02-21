package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.model.dao.LineNotifyDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.LineNotify;
import com.walnutek.fermentationtank.model.vo.DashboardDataVO;
import com.walnutek.fermentationtank.model.vo.LineNotifyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
public class LineNotifyService {

    @Autowired
    private LineNotifyDao lineNotifyDao;

    public List<DashboardDataVO> listAllGroupByLaboratoryId(
            List<String> userLabList,
            Map<String,String> userLabMap
    ){
        var lineNotifyQuery = List.of(
                where(LineNotify::getLaboratoryId).in(userLabList).build(),
                where(LineNotify::getStatus).is(BaseColumns.Status.ACTIVE).build()
        );
        var list = lineNotifyDao.selectList(lineNotifyQuery);
        Map<String, List<LineNotify>> map = list.stream()
                .collect(groupingBy(LineNotify::getLaboratoryId));
        var resulList = new ArrayList<DashboardDataVO>();
        for (String laboratoryId : map.keySet()) {
            if(userLabMap.containsKey(laboratoryId)){
                var vo = new DashboardDataVO();
                var laboratoryName = userLabMap.get(laboratoryId);
                vo.laboratory = laboratoryName;
                vo.laboratoryId = laboratoryId;
                var lineNotifyVOList = map.get(laboratoryId).stream().map(
                        lineNotify -> LineNotifyVO.of(lineNotify, laboratoryName)
                ).toList();
                vo.total = lineNotifyVOList.size();
                vo.data = lineNotifyVOList;
                resulList.add(vo);
            }
        }
        return resulList;
    }
}
