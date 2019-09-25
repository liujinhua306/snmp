package com.bluedon.snmp.nsfocus.ids;

import com.bluedon.snmp.common.AbstractSnmpClient;
import com.bluedon.snmp.common.Const;
import com.bluedon.snmp.entity.Nestatus;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author liujh
 * @date 2019/7/19 14:44
 */
@Slf4j
public class NsfocusClient extends AbstractSnmpClient {
    public NsfocusClient(String ip, int port, String community) {

        super(ip, port, community);
    }

    @Override
    public Nestatus collectCPU() {
        return readGet(".1.3.6.1.4.1.19849.6.2.2.0", Const.CPU).get(0);

    }

    @Override
    public Nestatus collectMemory() {
        return readGet(".1.3.6.1.4.1.19849.6.2.3.0", Const.MEMORY).get(0);


    }

    @Override
    public List<Nestatus> collectDisk() {

        return readGet(".1.3.6.1.4.1.19849.6.2.5.0", Const.DISK);

    }



    public List<Nestatus> readGet(String oid,String type){
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);
        ResponseEvent respEvnt = null;
        try {
            respEvnt = snmp.send(pdu, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Nestatus> datas = new ArrayList<>(16);

        // 解析Response
        if (respEvnt != null && respEvnt.getResponse() != null) {
            Vector<VariableBinding> recVBs = (Vector<VariableBinding>) respEvnt.getResponse().getVariableBindings();
            for (int i = 0; i < recVBs.size(); i++) {
                VariableBinding varBinding = recVBs.elementAt(i);
                log.info("[{}-{}-{}:{}={}]", ip,port,community,type,varBinding.getVariable());
                Nestatus nestatus = new Nestatus();
                nestatus.setStatusname(type);
                nestatus.setScanname("total");
                nestatus.setNumvalue(varBinding.getVariable().toString());
                datas.add(nestatus);
            }
        }


        return datas;
    }



    public static void main(String[] args) {
        NsfocusClient client = new NsfocusClient("172.16.110.61", 161, "public");
        client.collectCPU();
        client.collectMemory();
        client.collectDisk();

    }
}
