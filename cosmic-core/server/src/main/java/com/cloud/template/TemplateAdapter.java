package com.cloud.template;

import com.cloud.api.command.user.iso.DeleteIsoCmd;
import com.cloud.api.command.user.iso.RegisterIsoCmd;
import com.cloud.api.command.user.template.DeleteTemplateCmd;
import com.cloud.api.command.user.template.ExtractTemplateCmd;
import com.cloud.api.command.user.template.GetUploadParamsForTemplateCmd;
import com.cloud.api.command.user.template.RegisterTemplateCmd;
import com.cloud.legacymodel.communication.command.TemplateOrVolumePostUploadCommand;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.storage.TemplateType;
import com.cloud.legacymodel.user.Account;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.MaintenancePolicy;
import com.cloud.model.enumeration.OptimiseFor;
import com.cloud.storage.TemplateProfile;
import com.cloud.storage.VMTemplateVO;
import com.cloud.utils.component.Adapter;

import java.util.List;
import java.util.Map;

public interface TemplateAdapter extends Adapter {
    TemplateProfile prepare(RegisterTemplateCmd cmd) throws ResourceAllocationException;

    TemplateProfile prepare(GetUploadParamsForTemplateCmd cmd) throws ResourceAllocationException;

    TemplateProfile prepare(RegisterIsoCmd cmd) throws ResourceAllocationException;

    VMTemplateVO create(TemplateProfile profile);

    List<TemplateOrVolumePostUploadCommand> createTemplateForPostUpload(TemplateProfile profile);

    TemplateProfile prepareDelete(DeleteTemplateCmd cmd);

    TemplateProfile prepareDelete(DeleteIsoCmd cmd);

    TemplateProfile prepareExtractTemplate(ExtractTemplateCmd cmd);

    boolean delete(TemplateProfile profile);

    TemplateProfile prepare(boolean isIso, Long userId, String name, String displayText, Integer bits, Boolean passwordEnabled, String url, Boolean isPublic, Boolean featured,
                            Boolean isExtractable, String format, Long guestOSId, Long zoneId, HypervisorType hypervisorType, String accountName, Long domainId, String chksum,
                            Boolean bootable, Map details) throws ResourceAllocationException;

    TemplateProfile prepare(boolean isIso, long userId, String name, String displayText, Integer bits, Boolean passwordEnabled, String url, Boolean isPublic, Boolean featured,
                            Boolean isExtractable, String format, Long guestOSId, Long zoneId, HypervisorType hypervisorType, String chksum, Boolean bootable, String templateTag,
                            Account templateOwner, Map details, Boolean sshKeyEnabled, String imageStoreUuid, Boolean isDynamicallyScalable, TemplateType templateType,
                            String manufacturerString, OptimiseFor optimiseFor, MaintenancePolicy maintenancePolicy
    ) throws ResourceAllocationException;

    class TemplateAdapterType {
        public static final TemplateAdapterType Hypervisor = new TemplateAdapterType("HypervisorAdapter");
        String _name;

        public TemplateAdapterType(final String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }
    }
}
