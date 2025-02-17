package com.bluedon.snmp.snmptrap;


import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * 模拟Trap发送消息
 * @blog http://www.micmiu.com
 * @author Michael
 */
public class SnmpTrapSendDemo {

    public static final int DEFAULT_VERSION = SnmpConstants.version2c;
    public static final long DEFAULT_TIMEOUT = 3 * 1000L;
    public static final int DEFAULT_RETRY = 3;

    private Snmp snmp = null;
    private CommunityTarget target = null;

    public void init() throws IOException {
        System.out.println("----&gt; 初始 Trap 的IP和端口 &lt;----");
        target = createTarget4Trap("udp:10.130.10.8/162");
        TransportMapping transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
    }

    /**
     * 向接收器发送Trap 信息
     *
     * @throws IOException
     */
    public void sendPDU() throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(
                new OID(".1.3.6.1.2.1.1.1.0"),
                new OctetString("SNMP Trap Test.see more:http://www.micmiu.com")));
        pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(
                new UnsignedInteger32(System.currentTimeMillis() / 1000)
                        .getValue())));
        pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(
                ".1.3.6.1.6.3.1.1.4.3")));

        // 向Agent发送PDU
        pdu.setType(PDU.TRAP);
        snmp.send(pdu, target);
        System.out.println("----&gt; Trap Send END &lt;----");
    }

    /**
     * 创建对象communityTarget
     *
     * @return CommunityTarget
     */
    public static CommunityTarget createTarget4Trap(String address) {
        CommunityTarget target = new CommunityTarget();
        target.setAddress(GenericAddress.parse(address));
        target.setVersion(DEFAULT_VERSION);
        target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
        target.setRetries(DEFAULT_RETRY);
        target.setCommunity(new OctetString("public"));
        return target;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            SnmpTrapSendDemo demo = new SnmpTrapSendDemo();
            demo.init();
            demo.sendPDU();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
