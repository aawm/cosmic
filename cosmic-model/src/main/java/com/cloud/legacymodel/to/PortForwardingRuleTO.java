package com.cloud.legacymodel.to;

import com.cloud.legacymodel.network.FirewallRule;
import com.cloud.legacymodel.network.PortForwardingRule;

/**
 * PortForwardingRuleTO specifies one port forwarding rule.
 */
public class PortForwardingRuleTO extends FirewallRuleTO {
    String dstIp;
    int[] dstPortRange;

    protected PortForwardingRuleTO() {
        super();
    }

    public PortForwardingRuleTO(final PortForwardingRule rule, final String srcVlanTag, final String srcIp) {
        super(rule, srcVlanTag, srcIp);
        this.dstIp = rule.getDestinationIpAddress().addr();
        this.dstPortRange = new int[]{rule.getDestinationPortStart(), rule.getDestinationPortEnd()};
    }

    public PortForwardingRuleTO(final long id, final String srcIp, final int srcPortStart, final int srcPortEnd, final String dstIp, final int dstPortStart, final int
            dstPortEnd, final String protocol,
                                final boolean revoked, final boolean alreadyAdded) {
        super(id, null, srcIp, protocol, srcPortStart, srcPortEnd, revoked, alreadyAdded, FirewallRule.Purpose.PortForwarding, null, 0, 0);
        this.dstIp = dstIp;
        this.dstPortRange = new int[]{dstPortStart, dstPortEnd};
    }

    public String getDstIp() {
        return dstIp;
    }

    public int[] getDstPortRange() {
        return dstPortRange;
    }

    public String getStringDstPortRange() {
        return Integer.toString(dstPortRange[0]) + ":" + Integer.toString(dstPortRange[1]);
    }
}
