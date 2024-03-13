package com.walnutek.fermentationtank.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walnutek.fermentationtank.config.mongo.AggregationLookupBuilder;
import com.walnutek.fermentationtank.model.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.walnutek.fermentationtank.config.Const.LOOKUP_COLLECTION_DEVICE;

@Schema(title = "專案VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectVO extends BaseColumns {

    @Schema(title = "場域(實驗室Id)")
    private String laboratoryId;

    @Schema(title = "實驗室名稱")
    private String laboratory;

    @Schema(title = "專案名稱")
    private String name;

    @Schema(title = "目標裝置ID")
    private String deviceId;

    @Schema(title = "目標裝置名稱")
    private String device;

    @Schema(title = "起始時間")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime startTime;

    @Schema(title = "結束時間")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime endTime;

    public static ProjectVO of(Project data, String labName, String device) {
        var vo = new ProjectVO();
        vo.laboratoryId = data.getLaboratoryId();
        vo.laboratory = labName;
        vo.name = data.getName();
        vo.deviceId = data.getDeviceId();
        vo.device = device;
        vo.startTime = data.getStartTime();
        vo.endTime = data.getEndTime();
        syncBaseColumns(data, vo);

        return vo;
    }

    public Project toProject(Project data) {
        data.setDeviceId(deviceId);
        data.setName(name);
        data.setStartTime(startTime);
        data.setEndTime(endTime);

        updateBaseColumns(this, data);

        return data;
    }

    @JsonIgnore
    public static List<AggregationOperation> getLookupAggregation() {
        List<AggregationOperation> aggregationList = new ArrayList<>();
        aggregationList.addAll(getDeviceLookupAggregation());

        return aggregationList;
    }

    private static List<AggregationOperation> getDeviceLookupAggregation() {
        return AggregationLookupBuilder.from(Project.class)
                .outerJoin(Device.class)
                .on(Project::getDeviceId, Device::getId)
                .mappingTo(ProjectVO.class)
                .as(LOOKUP_COLLECTION_DEVICE)
                .asArrayField()
                .mapping(Device::getName, ProjectVO::getDevice)
                .build();
    }

}
