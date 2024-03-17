package com.walnutek.fermentationtank.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walnutek.fermentationtank.config.mongo.AggregationLookupBuilder;
import com.walnutek.fermentationtank.model.entity.*;
import com.walnutek.fermentationtank.model.entity.Alert.Condition;
import com.walnutek.fermentationtank.model.entity.Device.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.ArrayList;
import java.util.List;

import static com.walnutek.fermentationtank.config.Const.LOOKUP_COLLECTION_DEVICE;

@Schema(title = "警報設定VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class AlertVO extends BaseColumns {

    @Schema(title = "警報名稱")
    private String name;

    @Schema(title = "裝置分類")
    private DeviceType type = DeviceType.FERMENTER;

    @Schema(title = "目標裝置Id")
    @Field(targetType = FieldType.OBJECT_ID)
    private String deviceId;

    @Schema(title = "目標裝置")
    private String device;

    @Schema(title = "目標欄位")
    private String checkField;

    @Schema(title = "條件")
    private Condition condition;

    @Schema(title = "閥值")
    private Double threshold;

    @JsonIgnore
    public static List<AggregationOperation> getLookupAggregation() {
        List<AggregationOperation> aggregationList = new ArrayList<>();
        aggregationList.addAll(getDeviceLookupAggregation());

        return aggregationList;
    }

    private static List<AggregationOperation> getDeviceLookupAggregation() {
        return AggregationLookupBuilder.from(Alert.class)
                .outerJoin(Device.class)
                .on(Alert::getDeviceId, Device::getId)
                .mappingTo(AlertVO.class)
                .as(LOOKUP_COLLECTION_DEVICE)
                .asArrayField()
                .mapping(Device::getName, AlertVO::getDevice)
                .build();
    }
}
