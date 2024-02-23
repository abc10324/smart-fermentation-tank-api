package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.entity.Device.DeviceType;
import com.walnutek.fermentationtank.model.service.DeviceService;
import com.walnutek.fermentationtank.model.vo.DeviceVO;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Tag(name = "裝置")
@RestController
@RequestMapping("/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @Operation(summary = "取得裝置清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @Parameter(name = Const.KEYWORD, schema = @Schema(implementation = String.class), description = "裝置名稱")
    @Parameter(name = Const.PAGE, schema = @Schema(implementation = Integer.class), description = "頁數")
    @Parameter(name = Const.LIMIT, schema = @Schema(implementation = Integer.class), description = "每頁幾筆")
    @Parameter(name = Const.SORT_FIELD_KEY, schema = @Schema(implementation = String.class), description = "排序欄位")
    @Parameter(name = Const.SORT_DIRECTION_KEY, schema = @Schema(implementation = String.class), description = "排序方向", example = "asc|desc")
    @GetMapping("/{laboratoryId}/{type}")
    public Page<DeviceVO> search(@Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
                                 @Parameter(name = "type", description = "裝置類型") @PathVariable DeviceType type,
                                 @Parameter(hidden = true) @RequestParam Map<String, Object> paramMap){
        return deviceService.search(laboratoryId, type, paramMap);
    }

    @Operation(summary = "取得所有裝置清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping("/{laboratoryId}/{type}/list")
    public List<DeviceVO> getDeviceOptionListByLab(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "type", description = "裝置類型") @PathVariable DeviceType type
    ){
        return deviceService.list(laboratoryId, type);
    }

    @Operation(summary = "取得所有裝置類型")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping("/type-list")
    public List<DeviceType> getDeviceTypeList(){
        return Arrays.stream(DeviceType.values()).toList();
    }

    @Operation(summary = "新增裝置")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PostMapping("/{laboratoryId}")
    public Response createDevice(@Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
                                     @RequestBody DeviceVO vo) {
        deviceService.createDevice(laboratoryId, vo);
        return Response.ok();
    }

    @Operation(summary = "更新裝置")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PutMapping("/{laboratoryId}/{deviceId}")
    public Response updateDevice(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "deviceId", description = "裝置ID") @PathVariable String deviceId,
            @RequestBody DeviceVO vo) {
        deviceService.updateDevice(laboratoryId, deviceId, vo);
        return Response.ok();
    }

    @Operation(summary = "刪除裝置")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @DeleteMapping("/{laboratoryId}/{deviceId}")
    public Response deleteDevice(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "deviceId", description = "裝置ID") @PathVariable String deviceId
    ) {
        deviceService.deleteDevice(laboratoryId, deviceId);
        return Response.ok();
    }
}
