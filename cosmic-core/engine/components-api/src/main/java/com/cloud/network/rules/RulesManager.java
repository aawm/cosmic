package com.cloud.network.rules;

import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.exceptions.NetworkRuleConflictException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.FirewallRule;
import com.cloud.legacymodel.network.Nic;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.network.IpAddress;
import com.cloud.uservm.UserVm;

import java.util.List;

/**
 * Rules Manager manages the network rules created for different networks.
 */
public interface RulesManager extends RulesService {

    boolean applyPortForwardingRulesForNetwork(long networkId, boolean continueOnError, Account caller);

    boolean applyStaticNatRulesForNetwork(long networkId, boolean continueOnError, Account caller);

    void checkRuleAndUserVm(FirewallRule rule, UserVm userVm, Account caller);

    boolean revokeAllPFAndStaticNatRulesForIp(long ipId, long userId, Account caller) throws ResourceUnavailableException;

    boolean revokeAllPFStaticNatRulesForNetwork(long networkId, long userId, Account caller) throws ResourceUnavailableException;

    boolean revokePortForwardingRulesForVm(long vmId);

    FirewallRule[] reservePorts(IpAddress ip, String protocol, FirewallRule.Purpose purpose, boolean openFirewall, Account caller, int... ports)
            throws NetworkRuleConflictException;

    boolean applyStaticNatsForNetwork(long networkId, boolean continueOnError, Account caller);

    void getSystemIpAndEnableStaticNatForVm(VirtualMachine vm, boolean getNewIp) throws InsufficientAddressCapacityException;

    boolean disableStaticNat(long ipAddressId, Account caller, long callerUserId, boolean releaseIpIfElastic) throws ResourceUnavailableException;

    /**
     * @param networkId
     * @param continueOnError
     * @param caller
     * @param forRevoke
     * @return
     */
    boolean applyStaticNatForNetwork(long networkId, boolean continueOnError, Account caller, boolean forRevoke);

    List<FirewallRuleVO> listAssociatedRulesForGuestNic(Nic nic);
}
