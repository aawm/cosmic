package com.cloud.api.command.user.volume;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ExtractResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.legacymodel.storage.UploadStatus;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "extractVolume", group = APICommandGroup.VolumeService, description = "Extracts volume", responseObject = ExtractResponse.class, entityType = {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ExtractVolumeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ExtractVolumeCmd.class.getName());

    private static final String s_name = "extractvolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VolumeResponse.class,
            required = true, description = "the ID of the volume")
    private Long id;

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, required = false, length = 2048, description = "the url to which the volume would be extracted")
    private String url;

    @Parameter(name = ApiConstants.ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            required = true,
            description = "the ID of the zone where the volume is located")
    private Long zoneId;

    @Parameter(name = ApiConstants.MODE, type = CommandType.STRING, required = true, description = "the mode of extraction - HTTP_DOWNLOAD or FTP_UPLOAD")
    private String mode;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getStaticName() {
        return s_name;
    }

    public String getUrl() {
        return this.url;
    }

    public Long getZoneId() {
        return this.zoneId;
    }

    public String getMode() {
        return this.mode;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VOLUME_EXTRACT;
    }

    @Override
    public String getEventDescription() {
        return "Extraction job";
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    public Long getId() {
        return this.id;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Volume;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Volume Id: " + getId());
        final String uploadUrl = this._volumeService.extractVolume(this);
        if (uploadUrl != null) {
            final ExtractResponse response = new ExtractResponse();
            response.setResponseName(getCommandName());
            response.setObjectName("volume");
            final Volume vol = this._entityMgr.findById(Volume.class, this.id);
            response.setId(vol.getUuid());
            response.setName(vol.getName());
            final DataCenter zone = this._entityMgr.findById(DataCenter.class, this.zoneId);
            response.setZoneId(zone.getUuid());
            response.setZoneName(zone.getName());
            response.setMode(this.mode);
            response.setState(UploadStatus.DOWNLOAD_URL_CREATED.toString());
            final Account account = this._entityMgr.findById(Account.class, getEntityOwnerId());
            response.setAccountId(account.getUuid());
            response.setUrl(uploadUrl);
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to extract volume");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Volume volume = this._entityMgr.findById(Volume.class, getId());
        if (volume != null) {
            return volume.getAccountId();
        }

        // invalid id, parent this command to SYSTEM so ERROR events are tracked
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
