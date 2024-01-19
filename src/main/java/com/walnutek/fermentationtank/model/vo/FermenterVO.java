package com.walnutek.fermentationtank.model.vo;

import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Fermenter;
import com.walnutek.fermentationtank.model.entity.Fermenter.ConnectionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(title = "醱酵槽VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class FermenterVO extends BaseColumns {

    @Schema(title = "醱酵槽名稱")
    private String name;

    @Schema(title = "場域(實驗室Id)")
    private String laboratoryId;

    @Schema(title = "實驗室名稱")
    private String laboratory;

    @Schema(title = "連線狀態")
    private ConnectionStatus connectionStatus = ConnectionStatus.NORMAL;

    @Schema(title = "MAC ADDRESS")
    private String macAddress;

    @Schema(title = "醱酵槽狀態")
    private Status status = Status.ACTIVE;

    public static FermenterVO of(Fermenter data) {
        var vo = new FermenterVO();
        vo.name = data.getName();
        vo.laboratoryId = data.getLaboratoryId();
        vo.status = data.getStatus();
        vo.connectionStatus = data.getConnectionStatus();
        vo.macAddress = data.getMacAddress();
        vo.status = data.getStatus();

        syncBaseColumns(data, vo);

        return vo;
    }

    public Fermenter toFermenter() {
        var data = new Fermenter();
        data.setName(name);
        data.setLaboratoryId(laboratoryId);
        data.setConnectionStatus(connectionStatus);
        data.setMacAddress(macAddress);
        data.setStatus(status);

        syncBaseColumns(this, data);

        return data;
    }

}
