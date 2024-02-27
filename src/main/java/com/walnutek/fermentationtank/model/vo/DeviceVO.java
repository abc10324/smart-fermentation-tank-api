package com.walnutek.fermentationtank.model.vo;

import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Device;
import com.walnutek.fermentationtank.model.entity.Device.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Schema(title = "裝置VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceVO extends BaseColumns {

    @Schema(title = "裝置名稱")
    private String name;

    @Schema(title = "場域(實驗室Id)")
    @Field(targetType = FieldType.OBJECT_ID)
    private String laboratoryId;

    @Schema(title = "實驗室名稱")
    private String laboratory;

    @Schema(title = "連線狀態")
    private ConnectionStatus connectionStatus = ConnectionStatus.NORMAL;

    @Schema(title = "裝置分類")
    private DeviceType type = DeviceType.FERMENTER;

    @Schema(title = "裝置狀態")
    private Status status = Status.ACTIVE;

    public static DeviceVO of(Device data, String labName, ConnectionStatus connectionStatus) {
        var vo = new DeviceVO();
        vo.name = data.getName();
        vo.laboratory = labName;
        vo.laboratoryId = data.getLaboratoryId();
        vo.connectionStatus = connectionStatus;
        vo.type = data.getType();
        vo.status = data.getStatus();

        syncBaseColumns(data, vo);

        return vo;
    }

    public Device toDevice(Device data) {
        data.setName(name);
        data.setLaboratoryId(laboratoryId);
        data.setType(type);

        updateBaseColumns(this, data);

        return data;
    }

    @Getter
    public enum ConnectionStatus {
        NORMAL( "正常"),
        EXCEPTION("異常");

        private String name;

        ConnectionStatus(String name) {
            this.name = name;
        }
    }
}
