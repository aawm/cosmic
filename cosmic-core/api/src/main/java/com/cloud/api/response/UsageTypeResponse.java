package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

public class UsageTypeResponse extends BaseResponse {

    @SerializedName("usagetypeid")
    @Param(description = "usage type")
    private Integer usageType;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "description of usage type")
    private String description;

    public UsageTypeResponse(final Integer usageType, final String description) {
        this.usageType = usageType;
        this.description = description;
        setObjectName("usagetype");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Integer getUsageType() {
        return usageType;
    }

    public void setUsageType(final Integer usageType) {
        this.usageType = usageType;
    }
}
