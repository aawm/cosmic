package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.serializer.Param;
import com.cloud.vm.snapshot.VMSnapshot;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = VMSnapshot.class)
public class VMSnapshotResponse extends BaseResponse implements ControlledEntityResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the vm snapshot")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the vm snapshot")
    private String name;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the vm snapshot")
    private VMSnapshot.State state;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "the description of the vm snapshot")
    private String description;

    @SerializedName(ApiConstants.DISPLAY_NAME)
    @Param(description = "the display name of the vm snapshot")
    private String displayName;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the Zone ID of the vm snapshot")
    private String zoneId;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID)
    @Param(description = "the vm ID of the vm snapshot")
    private String virtualMachineid;

    @SerializedName("parent")
    @Param(description = "the parent ID of the vm snapshot")
    private String parent;

    @SerializedName("parentName")
    @Param(description = "the parent displayName of the vm snapshot")
    private String parentName;

    @SerializedName("current")
    @Param(description = "indiates if this is current snapshot")
    private Boolean current;

    @SerializedName("type")
    @Param(description = "VM Snapshot type")
    private String type;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the create date of the vm snapshot")
    private Date created;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account associated with the disk volume")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the vpn")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the vpn")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the ID of the domain associated with the disk volume")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain associated with the disk volume")
    private String domainName;

    @Override
    public String getObjectId() {
        return getId();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public String getVirtualMachineid() {
        return virtualMachineid;
    }

    public void setVirtualMachineid(final String virtualMachineid) {
        this.virtualMachineid = virtualMachineid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public VMSnapshot.State getState() {
        return state;
    }

    public void setState(final VMSnapshot.State state) {
        this.state = state;
    }

    public Boolean getCurrent() {
        return current;
    }

    public void setCurrent(final Boolean current) {
        this.current = current;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(final String parentName) {
        this.parentName = parentName;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }
}
