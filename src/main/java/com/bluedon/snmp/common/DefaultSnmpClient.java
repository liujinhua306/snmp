package com.bluedon.snmp.common;

/**
 * @author liujh
 * @date 2019/7/19 9:38
 */
public class DefaultSnmpClient extends AbstractSnmpClient {

    public DefaultSnmpClient(String ip, int port, String community) {
        super(ip, port, community);
    }

    public static void main(String[] args) {
        DefaultSnmpClient testDemo = new DefaultSnmpClient("172.16.110.164", 161, "public");
        testDemo.collectDisk();
//        testDemo.collectCPU();
//        testDemo.collectMemory();

    }
}
