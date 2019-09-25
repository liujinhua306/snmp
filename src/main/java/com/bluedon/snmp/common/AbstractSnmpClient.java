package com.bluedon.snmp.common;

import com.bluedon.snmp.entity.Nestatus;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import org.snmp4j.util.TreeUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liujh
 * @date 2019/7/19 9:38
 */
public abstract class AbstractSnmpClient {

    protected Snmp snmp = null;
    protected CommunityTarget target = null;
    private static long timeout = 8000;
    protected TableUtils tableUtils = null;
    protected TreeUtils treeUtils = null;
    protected String ip ;
    protected int port ;
    protected String community ;
    //获取CPU的使用率

    public AbstractSnmpClient(String ip, int port, String community) {
        this.ip = ip ;
        this.port = port ;
        this.community = community ;
        port = port == 0 ? 161 : port;
        community = community == null || "".equals(community) ? "public" : community;
        initComm(ip, port, community);
    }

    private void initComm(String ip, int port, String community) {
        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);//创建snmp
            snmp.listen();//监听消息
        } catch (IOException e) {
            e.printStackTrace();
        }

        target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setRetries(2);
        target.setAddress(GenericAddress.parse("udp:" + ip + "//" + port));
        target.setTimeout(timeout);
        target.setVersion(SnmpConstants.version2c);

        tableUtils = new TableUtils(snmp, new DefaultPDUFactory());
        treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
    }

    public Nestatus collectCPU() {
        int percentage = 0;
        String[] oidsCpu = {"1.3.6.1.2.1.25.3.3.1.2"};
        OID[] columns = new OID[oidsCpu.length];
        for (int i = 0; i < oidsCpu.length; i++) {
            columns[i] = new OID(oidsCpu[i]);
        }

        List<TableEvent> list = null;
        try {
            list = tableUtils.getTable(target, columns, null, null);
            if (list.size() == 1 && list.get(0).getColumns() == null) {

            } else {
                for (TableEvent event : list) {
                    VariableBinding[] values = event.getColumns();
                    if (values != null) {
                        for (VariableBinding var : values) {
                            System.out.println(var);
                        }
                        percentage += Integer.parseInt(values[0].getVariable().toString());
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            try {
                if (snmp != null){

                    snmp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("CPU利用率为：" + percentage / list.size() + "%");
        Nestatus nestatus = new Nestatus();
        nestatus.setStatusname(Const.CPU);
        nestatus.setScanname("total");
        nestatus.setNumvalue(percentage / list.size()+"");
        return nestatus;
    }

    //获取内存相关信息
    public Nestatus collectMemory() {

        Snmp snmp = null;
        String[] oids = {"1.3.6.1.2.1.25.2.3.1.2",  //type 存储单元类型
                "1.3.6.1.2.1.25.2.3.1.3",  //descr
                "1.3.6.1.2.1.25.2.3.1.4",  //unit 存储单元大小
                "1.3.6.1.2.1.25.2.3.1.5",  //size 总存储单元数
                "1.3.6.1.2.1.25.2.3.1.6"}; //used 使用存储单元数;
        String PHYSICAL_MEMORY_OID = "1.3.6.1.2.1.25.2.1.2";//物理存储
        String VIRTUAL_MEMORY_OID = "1.3.6.1.2.1.25.2.1.3"; //虚拟存储
        int usedSize = 0;
        int totalSize = 0;
        try {
            OID[] columns = new OID[oids.length];
            for (int i = 0; i < oids.length; i++) {
                columns[i] = new OID(oids[i]);
            }

            List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
            if (list.size() == 1 && list.get(0).getColumns() == null) {
                System.out.println(" null");
            } else {
                for (TableEvent event : list) {
                    VariableBinding[] values = event.getColumns();
                    if (values == null) continue;
                    int unit = Integer.parseInt(values[2].getVariable().toString());//unit 存储单元大小
                    totalSize = Integer.parseInt(values[3].getVariable().toString());//size 总存储单元数

                    if (values[4] != null) {
                        usedSize = Integer.parseInt(values[4].getVariable().toString());//used  使用存储单元数
                    }
                    String oid = values[0].getVariable().toString();
                    if (PHYSICAL_MEMORY_OID.equals(oid)) {
                        System.out.println("PHYSICAL_MEMORY----->物理内存大小：" + (long) totalSize * unit / (1024 * 1024 * 1024) + "G   内存使用率为：" + (long) usedSize * 100 / totalSize + "%");
                    } else if (VIRTUAL_MEMORY_OID.equals(oid)) {
                        System.out.println("VIRTUAL_MEMORY----->虚拟内存大小：" + (long) totalSize * unit / (1024 * 1024 * 1024) + "G   内存使用率为：" + (long) usedSize * 100 / totalSize + "%");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {

                if (snmp != null){

                    snmp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Nestatus nestatus = new Nestatus();
        nestatus.setStatusname(Const.MEMORY);
        nestatus.setScanname("total");
        nestatus.setNumvalue((long) usedSize * 100 / totalSize + "");
        return nestatus;

    }

    //获取磁盘相关信息
    public List<Nestatus> collectDisk() {
        List<Nestatus> datas = new ArrayList<>(16);
        Snmp snmp = null;
        String DISK_OID = "1.3.6.1.2.1.25.2.1.4";
        String[] oids = {"1.3.6.1.2.1.25.2.3.1.2",  //type 存储单元类型
                "1.3.6.1.2.1.25.2.3.1.3",  //descr
                "1.3.6.1.2.1.25.2.3.1.4",  //unit 存储单元大小
                "1.3.6.1.2.1.25.2.3.1.5",  //size 总存储单元数
                "1.3.6.1.2.1.25.2.3.1.6"}; //used 使用存储单元数;
        try {

            OID[] columns = new OID[oids.length];
            for (int i = 0; i < oids.length; i++) {

                columns[i] = new OID(oids[i]);
            }
            List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
            if (list.size() == 1 && list.get(0).getColumns() == null) {
                System.out.println(" null");
            } else {
                for (TableEvent event : list) {
                    VariableBinding[] values = event.getColumns();
                    if (values == null || !DISK_OID.equals(values[0].getVariable().toString()))
                        continue;
                    int unit = Integer.parseInt(values[2].getVariable().toString());//unit 存储单元大小
                    int totalSize = Integer.parseInt(values[3].getVariable().toString());//size 总存储单元数
                    int usedSize = Integer.parseInt(values[4].getVariable().toString());//used  使用存储单元数
                    System.out.println(getChinese(values[1].getVariable().toString()) + "   磁盘大小：" + (long) totalSize * unit / (1024 * 1024) + "M   磁盘使用率为：" + (long) usedSize * 100 / totalSize + "%");
                    Nestatus nestatus = new Nestatus();
                    nestatus.setStatusname(Const.DISK);
                    nestatus.setScanname(getChinese(values[1].getVariable().toString()));
                    nestatus.setNumvalue((long) usedSize * 100 / totalSize + "");
                    datas.add(nestatus);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {

                if (snmp != null){
                    snmp.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return datas;

    }


    public List<Nestatus> collectSoftware() {
        TransportMapping transport = null;
        Snmp snmp = null;
        String DISK_OID = "1.3.6.1.2.1.25.2.1.4";
        String[] oids = {".1.3.6.1.2.1.25.6.3.1.1",
                ".1.3.6.1.2.1.25.6.3.1.2",
                ".1.3.6.1.2.1.25.6.3.1.3",
                ".1.3.6.1.2.1.25.6.3.1.4",
                ".1.3.6.1.2.1.25.6.3.1.5"};
        try {

            OID[] columns = new OID[oids.length];
            for (int i = 0; i < oids.length; i++) {

                columns[i] = new OID(oids[i]);
            }
            @SuppressWarnings("unchecked")
            List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
            if (list.size() == 1 && list.get(0).getColumns() == null) {
                System.out.println(" null");
            } else {
                for (TableEvent event : list) {
                    VariableBinding[] values = event.getColumns();
                    if (values == null || !DISK_OID.equals(values[0].getVariable().toString()))
                        continue;
                    int unit = Integer.parseInt(values[2].getVariable().toString());//unit 存储单元大小
                    int totalSize = Integer.parseInt(values[3].getVariable().toString());//size 总存储单元数
                    int usedSize = Integer.parseInt(values[4].getVariable().toString());//used  使用存储单元数
                    System.out.println(getChinese(values[1].getVariable().toString()) + "   磁盘大小：" + (long) totalSize * unit / (1024 * 1024) + "M   磁盘使用率为：" + (long) usedSize * 100 / totalSize + "%");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (transport != null)
                    transport.close();
                if (snmp != null)
                    snmp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;

    }


    /**
     * 获取磁盘的中文名字
     * 解决snmp4j中文乱码问题
     */
    public static String getChinese(String octetString) {
        if (octetString == null || "".equals(octetString)
                || "null".equalsIgnoreCase(octetString)) return "";
        try {
            String[] temps = octetString.split(":");
            if (temps.length < 10)
                return octetString;
            byte[] bs = new byte[temps.length];
            for (int i = 0; i < temps.length; i++)
                bs[i] = (byte) Integer.parseInt(temps[i], 16);
            return new String(bs, "gb2312");
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {

    }
}
