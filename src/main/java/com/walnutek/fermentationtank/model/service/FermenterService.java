package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.FermenterDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Fermenter;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.FermenterVO;
import com.walnutek.fermentationtank.model.entity.Fermenter.ConnectionStatus;
import com.walnutek.fermentationtank.model.entity.BaseColumns.Status;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;

@Service
@Transactional
public class FermenterService extends BaseService {

    @Autowired
    private FermenterDao fermenterDao;

    public Page<FermenterVO> search(String laboratoryId, Map<String, Object> paramMap) {
        var resultPage = fermenterDao.search(laboratoryId, paramMap);
        var lab = laboratoryDao.selectById(laboratoryId);
        resultPage.getRecords().stream().forEach(fermenter -> fermenter.setLaboratory(lab.getName()));
        return resultPage;
    }

    public List<FermenterVO> list(String laboratoryId) {
        var query = List.of(
                where(Fermenter::getLaboratoryId).is(laboratoryId).build(),
                where(Fermenter::getStatus).is(Status.ACTIVE).build());
        return fermenterDao.selectList(query).stream().map(FermenterVO::of).toList();
    }

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

    public void updateFermenter(String laboratoryId, String fermenterId, FermenterVO vo) {
        isFermenterAvailableEdit(laboratoryId, fermenterId);
        var data = fermenterDao.selectById(fermenterId);
        data.setName(vo.getName());
        data.setLaboratoryId(vo.getLaboratoryId());
        data.setConnectionStatus(vo.getConnectionStatus());
        data.setMacAddress(vo.getMacAddress());
        fermenterDao.updateById(data);
    }

    public void deleteFermenter(String laboratoryId, String fermenterId){
        isFermenterAvailableEdit(laboratoryId, fermenterId);
        var data = fermenterDao.selectById(fermenterId);
        data.setStatus(BaseColumns.Status.DELETED);
        fermenterDao.updateById(data);
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
