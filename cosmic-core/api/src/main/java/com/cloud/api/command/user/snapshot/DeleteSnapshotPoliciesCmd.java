package com.cloud.api.command.user.snapshot;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SnapshotPolicyResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.user.Account;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteSnapshotPolicies", description = "Deletes snapshot policies for the account.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteSnapshotPoliciesCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteSnapshotPoliciesCmd.class.getName());

    private static final String s_name = "deletesnapshotpoliciesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = SnapshotPolicyResponse.class, description = "the Id of the snapshot policy")
    private Long id;

    @Parameter(name = ApiConstants.IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = SnapshotPolicyResponse.class,
            description = "list of snapshots policy IDs separated by comma")
    private List<Long> ids;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public List<Long> getIds() {
        return ids;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean result = _snapshotService.deleteSnapshotPolicies(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete snapshot policy");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
