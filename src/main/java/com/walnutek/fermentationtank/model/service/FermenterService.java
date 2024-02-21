package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.FermenterDao;
import com.walnutek.fermentationtank.model.dao.LaboratoryDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Fermenter;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.DashboardDataVO;
import com.walnutek.fermentationtank.model.vo.FermenterVO;
import com.walnutek.fermentationtank.model.entity.Fermenter.ConnectionStatus;
import com.walnutek.fermentationtank.model.entity.BaseColumns.Status;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
public class FermenterService extends BaseService {

    @Autowired
    private FermenterDao fermenterDao;

    @Autowired
    private LaboratoryDao laboratoryDao;

    public String createFermenter(String laboratoryId, FermenterVO vo) {
        var user = getLoginUser();
        if(User.Role.SUPER_ADMIN.equals(user.getRole())){
            throw new AppException(AppException.Code.E002, "此帳號無權限建立醱酵槽");
        }
        if(StringUtils.hasText(vo.getName())) {
            var data = vo.toFermenter();
            data.setName(vo.getName());
            data.setLaboratoryId(laboratoryId);
            data.setConnectionStatus(ConnectionStatus.EXCEPTION);
            data.setStatus(Status.ACTIVE);
            data.setMacAddress(vo.getMacAddress());
            fermenterDao.insert(data);

            return data.getId();
        } else {
            throw new AppException(AppException.Code.E002, "必填欄位資料不正確");
        }
    }

    public void deleteFermenter(String laboratoryId, String fermenterId){
        isFermenterAvailableEdit(laboratoryId, fermenterId);
        var data = fermenterDao.selectById(fermenterId);
        data.setStatus(BaseColumns.Status.DELETED);
        fermenterDao.updateById(data);
    }

    public void updateFermenter(String laboratoryId, String fermenterId, FermenterVO vo) {
        isFermenterAvailableEdit(laboratoryId, fermenterId);
        var data = fermenterDao.selectById(fermenterId);
        data.setName(vo.getName());
        data.setLaboratoryId(vo.getLaboratoryId());
        data.setConnectionStatus(vo.getConnectionStatus());
        data.setMacAddress(vo.getMacAddress());
        fermenterDao.updateById(data);
    }

    public Page<FermenterVO> search(String laboratoryId, Map<String, Object> paramMap) {
        var resultPage = fermenterDao.search(laboratoryId, paramMap);
        var lab = laboratoryDao.selectById(laboratoryId);
        resultPage.getRecords().stream().forEach(fermenter -> fermenter.setLaboratory(lab.getName()));
        return resultPage;
    }

    public List<FermenterVO> list(String laboratoryId) {
        var laboratory = Optional.ofNullable(laboratoryDao.selectById(laboratoryId))
                .orElseThrow(() -> new AppException(AppException.Code.E004));
        var labName = laboratory.getName();
        var query = List.of(
                where(Fermenter::getLaboratoryId).is(laboratoryId).build(),
                where(Fermenter::getStatus).is(Status.ACTIVE).build());
        return fermenterDao.selectList(query).stream().map(fermenter -> FermenterVO.of(fermenter, labName)).toList();
    }

//    public Map<String, List<FermenterVO>> listAllGroupByLaboratoryId(
//            List<String> userLabList, Map<String,String> userLabMap){
//        var fermenterQuery = List.of(
//                where(Fermenter::getLaboratoryId).in(userLabList).build(),
//                where(Fermenter::getStatus).is(Status.ACTIVE).build()
//        );
//        Map<String, List<Fermenter>> map = fermenterDao.selectList(fermenterQuery).stream()
//                .collect(groupingBy(Fermenter::getLaboratoryId));
//        var resultMap = new HashMap<String, List<FermenterVO>>();
//        for (String laboratoryId : map.keySet()) {
//            if(userLabMap.containsKey(laboratoryId)){
//                var laboratoryName = userLabMap.get(laboratoryId);
//                var fermenterList = map.get(laboratoryId).stream().map(fermenter -> FermenterVO.of(fermenter, laboratoryName)).toList();
//                resultMap.put(laboratoryName, fermenterList);
//            }
//        }
//        return resultMap;
//    }
    public List<DashboardDataVO> listAllGroupByLaboratoryId(
            List<String> userLabList, Map<String,String> userLabMap){
        var fermenterQuery = List.of(
                where(Fermenter::getLaboratoryId).in(userLabList).build(),
                where(Fermenter::getStatus).is(Status.ACTIVE).build()
        );
        Map<String, List<Fermenter>> map = fermenterDao.selectList(fermenterQuery).stream()
                .collect(groupingBy(Fermenter::getLaboratoryId));
        var resulList = new ArrayList<DashboardDataVO>();
        for (String laboratoryId : map.keySet()) {
            if(userLabMap.containsKey(laboratoryId)){
                var vo = new DashboardDataVO();
                var laboratoryName = userLabMap.get(laboratoryId);
                vo.laboratory = laboratoryName;
                vo.laboratoryId = laboratoryId;
                var fermenterVOList = map.get(laboratoryId).stream().map(fermenter -> FermenterVO.of(fermenter, laboratoryName)).toList();
                vo.total = fermenterVOList.size();
                vo.data = fermenterVOList;
                resulList.add(vo);
            }
        }
        return resulList;
    }

    public List<Fermenter> listByQuery(List<Criteria> criteriaList){
        return fermenterDao.selectList(criteriaList);
    }

    public Integer countFermenterNum(String laboratoryId){
        var query = List.of(
                where(Fermenter::getLaboratoryId).is(laboratoryId).build(),
                where(Fermenter::getStatus).is(Status.ACTIVE).build());
        return Math.toIntExact(fermenterDao.count(query));
    }

    public Integer countFermenterNum(List<String> userLabList){
        var query = List.of(
                where(Fermenter::getLaboratoryId).in(userLabList).build(),
                where(Fermenter::getStatus).is(Status.ACTIVE).build());
        return Math.toIntExact(fermenterDao.count(query));
    }
    private void isFermenterAvailableEdit(String laboratoryId, String fermenterId) {
        var fermenter = fermenterDao.selectByIdAndStatus(fermenterId, BaseColumns.Status.ACTIVE);
        if(Objects.isNull(fermenter)){
            throw new AppException(AppException.Code.E002, "無法更新不存在的醱酵槽");
        }
        var userLabList = getUserLabList();
        if(!userLabList.contains(laboratoryId)){
            throw new AppException(AppException.Code.E002, "非所屬實驗室人員無法編輯");
        }
    }
}
