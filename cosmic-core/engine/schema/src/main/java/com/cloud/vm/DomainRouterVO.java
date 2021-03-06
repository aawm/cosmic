package com.cloud.vm;

import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.MaintenancePolicy;
import com.cloud.model.enumeration.OptimiseFor;
import com.cloud.model.enumeration.VirtualMachineType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 */
@Entity
@Table(name = "domain_router")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue(value = "DomainRouter")
public class DomainRouterVO extends VMInstanceVO implements VirtualRouter {
    @Column(name = "is_redundant_router")
    boolean isRedundantRouter;
    @Column(name = "stop_pending")
    boolean stopPending;
    @Column(name = "element_id")
    private long elementId;
    @Column(name = "public_ip_address")
    private String publicIpAddress;
    @Column(name = "public_mac_address")
    private String publicMacAddress;
    @Column(name = "public_netmask")
    private String publicNetmask;
    @Column(name = "redundant_state")
    @Enumerated(EnumType.STRING)
    private RedundantState redundantState;
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role = Role.VIRTUAL_ROUTER;

    @Column(name = "template_version")
    private String templateVersion;

    @Column(name = "scripts_version")
    private String scriptsVersion;

    @Column(name = "vpc_id")
    private Long vpcId;

    @Column(name = "router_unicast_id")
    private Long routerUnicastId;

    public DomainRouterVO(final long id, final long serviceOfferingId, final long elementId, final String name, final long templateId, final HypervisorType hypervisorType,
                          final long guestOSId, final long domainId, final long accountId, final long userId, final boolean isRedundantRouter, final RedundantState redundantState,
                          final boolean haEnabled, final boolean stopPending, final Long vpcId, final OptimiseFor optimiseFor, final String manufacturerString,
                          final String cpuFlags, final Boolean macLearning, final Boolean requiresRestart, final MaintenancePolicy maintenancePolicy, final Long routerUnicastId) {
        super(id, serviceOfferingId, name, name, VirtualMachineType.DomainRouter, templateId, hypervisorType, guestOSId, domainId, accountId, userId, haEnabled);
        this.elementId = elementId;
        this.isRedundantRouter = isRedundantRouter;
        this.redundantState = redundantState;
        this.stopPending = stopPending;
        this.vpcId = vpcId;
        this.optimiseFor = optimiseFor;
        this.manufacturerString = manufacturerString;
        this.cpuFlags = cpuFlags;
        this.macLearning = macLearning;
        this.requiresRestart = requiresRestart;
        this.maintenancePolicy = maintenancePolicy;
        this.routerUnicastId = routerUnicastId;
    }

    protected DomainRouterVO() {
        super();
    }

    public long getElementId() {
        return elementId;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    @Override
    public long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public String getPublicNetmask() {
        return publicNetmask;
    }

    public void setPublicNetmask(final String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public String getPublicMacAddress() {
        return publicMacAddress;
    }

    public void setPublicMacAddress(final String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    @Override
    public Role getRole() {
        return role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }

    @Override
    public boolean getIsRedundantRouter() {
        return isRedundantRouter;
    }

    public void setIsRedundantRouter(final boolean isRedundantRouter) {
        this.isRedundantRouter = isRedundantRouter;
    }

    @Override
    public RedundantState getRedundantState() {
        return redundantState;
    }

    @Override
    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(final String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    @Override
    public boolean isStopPending() {
        return stopPending;
    }

    @Override
    public void setStopPending(final boolean stopPending) {
        this.stopPending = stopPending;
    }

    @Override
    public Long getVpcId() {
        return vpcId;
    }

    @Override
    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(final String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public void setRedundantState(final RedundantState redundantState) {
        this.redundantState = redundantState;
    }

    public String getScriptsVersion() {
        return scriptsVersion;
    }

    public void setScriptsVersion(final String scriptsVersion) {
        this.scriptsVersion = scriptsVersion;
    }

    public Long getRouterUnicastId() {
        return routerUnicastId;
    }

    public void setRouterUnicastId(final Long routerUnicastId) {
        this.routerUnicastId = routerUnicastId;
    }
}
