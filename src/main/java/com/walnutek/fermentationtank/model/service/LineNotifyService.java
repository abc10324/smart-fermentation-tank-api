package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.DeviceDao;
import com.walnutek.fermentationtank.model.dao.LineNotifyDao;
import com.walnutek.fermentationtank.model.entity.*;
import com.walnutek.fermentationtank.model.vo.DashboardDataVO;
import com.walnutek.fermentationtank.model.vo.LineNotifyVO;
import com.walnutek.fermentationtank.model.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;
import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Service
@Transactional
public class LineNotifyService extends BaseService {

    @Autowired
    private LineNotifyDao lineNotifyDao;

    @Autowired
    private DeviceDao deviceDao;
    public static final String LOCAL_LINE_NOTIFY_API = "/api/line-notify";
    @Value("${app.line-notify.token-api}")
    public String LINE_TOKEN_API;
    @Value("${app.line-notify.notify-api}")
    public String LINE_NOTIFY_API;
    public static final String QUERY_QUESTION = "?";
    public static final String QUERY_AND = "&";
    public static final String QUERY_UNDER_SCORE = "_";
    public static final String QUERY_ID = "id=";
    public static final String QUERY_GRANT_TYPE = "grant_type=authorization_code";
    public static final String QUERY_CLIENT_ID = "client_id=";
    @Value("${app.line-notify.client-id}")
    public String CLIENT_ID;
    public static final String QUERY_CLIENT_SECRET = "client_secret=";
    @Value("${app.line-notify.client-secret}")
    public String CLIENT_SECRET;
    public static final String QUERY_CODE = "code=";
    public static final String QUERY_REDIRECT_URI = "redirect_uri=";

    public String createLineNotify(String laboratoryId, String userId, String baseUrl, String code, String state){
        var redirectUri = baseUrl + LOCAL_LINE_NOTIFY_API + QUERY_QUESTION
                + QUERY_ID + laboratoryId + QUERY_UNDER_SCORE + userId;
        var paramMap = new HashMap<String,Object>();
        paramMap.put("laboratoryId", laboratoryId);
        paramMap.put("userId", userId);
        var lineNotify = getLineNotify(paramMap);
        var isCreate = lineNotify == null;
        if(isCreate){
            lineNotify = new LineNotify();
            lineNotify.setLaboratoryId(laboratoryId);
            lineNotify.setUserId(userId);
            lineNotify.setRedirectUri(redirectUri);
            lineNotify.setCode(code);
            lineNotify.setState(state);
            var accessToken = getAccessToken(redirectUri, code);
            lineNotify.setAccessToken(accessToken);
            lineNotify.setStatus(BaseColumns.Status.ACTIVE);
            lineNotifyDao.insert(lineNotify);
        }
        return lineNotify.getId();
    }

    public void updateLineNotify(String laboratoryId, String lineNotifyId, LineNotifyVO vo){
        var paramMap = new HashMap<String,Object>();
        paramMap.put("laboratoryId", laboratoryId);
        paramMap.put("lineNotifyId", lineNotifyId);
        var lineNotify = Optional.ofNullable(getLineNotify(paramMap))
                .orElseThrow(() -> new AppException(AppException.Code.E004));
        lineNotify.setStatus(vo.getStatus());
        lineNotify.setUpdateTime(LocalDateTime.now());
        lineNotify.setUpdateUser(getLoginUserId());
        lineNotifyDao.updateById(lineNotify);
    }

    private String getAccessToken(String redirectUri, String code){
        RestClient restClient = RestClient.create();
        var uri = LINE_TOKEN_API + QUERY_QUESTION
                + QUERY_GRANT_TYPE
                + QUERY_AND + QUERY_CLIENT_ID + CLIENT_ID
                + QUERY_AND + QUERY_CLIENT_SECRET + CLIENT_SECRET
                + QUERY_AND + QUERY_CODE + code
                + QUERY_AND + QUERY_REDIRECT_URI + redirectUri;
        LineAccessToken response = restClient.post()
                .uri(uri)
                .contentType(APPLICATION_JSON)
                .retrieve()
                .body(LineAccessToken.class);
        if(response.getStatus() == 200){
            return response.getAccess_token();
        }else {
            throw new AppException(AppException.Code.E000);
        }
    }

    public void sendLineNotify(String laboratoryId, Alert alert, AlertRecord alertRecord){
        var laboratory = Optional.ofNullable(laboratoryDao.selectByIdAndStatus(laboratoryId, BaseColumns.Status.ACTIVE))
                .orElseThrow(() -> new AppException(AppException.Code.E004));
        var device = Optional.ofNullable(deviceDao.selectByIdAndStatus(alert.getDeviceId(), BaseColumns.Status.ACTIVE))
                .orElseThrow(() -> new AppException(AppException.Code.E004));

        var lineNotifyQuery = List.of(
                where(LineNotify::getLaboratoryId).is(laboratoryId).build(),
                where(LineNotify::getStatus).is(BaseColumns.Status.ACTIVE).build()
        );
        var lineNotifyList = lineNotifyDao.selectList(lineNotifyQuery);
        if(!lineNotifyList.isEmpty()){
            RestClient restClient = RestClient.create();
            lineNotifyList.forEach(lineNotify -> {
                MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
                var message = "警報通知 - "
                            + "\n 實驗室：" + laboratory.getName()
                            + "\n 警報名稱：" + alert.getName()
                            + "\n 目標裝置：" + device.getName()
                            + "\n 目標欄位：" + alert.getCheckField()
                            + "\n 條件：" + alert.getCondition().getName()
                            + "\n 閥值：" + alert.getThreshold()
                            + "\n 異常值：" + alertRecord.getTriggerValue();
                data.add("message", message);

                ResponseEntity<Void> response = restClient.post()
                        .uri(LINE_NOTIFY_API)
                        .contentType(MULTIPART_FORM_DATA)
                        .header("Authorization",
                                "Bearer "+lineNotify.getAccessToken())
                        .body(data)
                        .retrieve()
                        .toBodilessEntity();
            });
        }
    }

    public Page<LineNotifyVO> search(String laboratoryId, Map<String, Object> paramMap) {
        paramMap.put("laboratoryId", laboratoryId);
        return lineNotifyDao.search(paramMap);
    }

    public List<DashboardDataVO> listAllGroupByLaboratoryId(
            List<String> userLabList,
            Map<String,String> userLabMap
    ){
        var paramMap = new HashMap<String,Object>();
        paramMap.put("userLabList", userLabList);
        var list = lineNotifyDao.searchAsList(paramMap);
        Map<String, List<LineNotifyVO>> map = list.stream()
                .collect(groupingBy(LineNotifyVO::getLaboratoryId));
        var resulList = new ArrayList<DashboardDataVO>();
        for (String laboratoryId : map.keySet()) {
            if(userLabMap.containsKey(laboratoryId)){
                var vo = new DashboardDataVO();
                var laboratoryName = userLabMap.get(laboratoryId);
                vo.laboratory = laboratoryName;
                vo.laboratoryId = laboratoryId;
                var lineNotifyVOList = map.get(laboratoryId).stream()
                    .map(lineNotify -> {
                        lineNotify.setLaboratory(laboratoryName);
                        return lineNotify;
                    }).toList();
                vo.total = lineNotifyVOList.size();
                vo.data = lineNotifyVOList;
                resulList.add(vo);
            }
        }
        return resulList;
    }

    public LineNotify getLineNotify(Map<String, Object> paramMap){
        var lineNotifyQuery = Stream.of(
                        where(hasText(paramMap.get("lineNotifyId")), LineNotify::getId).is(paramMap.get("lineNotifyId")),
                        where(hasText(paramMap.get("laboratoryId")), LineNotify::getLaboratoryId).is(paramMap.get("laboratoryId")),
                        where(hasText(paramMap.get("userId")), LineNotify::getUserId).is(paramMap.get("userId"))
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        return lineNotifyDao.selectOne(lineNotifyQuery);
    }
}


