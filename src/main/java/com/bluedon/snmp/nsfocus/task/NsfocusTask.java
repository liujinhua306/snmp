package com.bluedon.snmp.nsfocus.task;

import com.bluedon.snmp.entity.Nestatus;
import com.bluedon.snmp.nsfocus.ids.NsfocusClient;
import com.bluedon.snmp.service.SnmpService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author liujh
 * @date 2019/9/25 8:42
 */
@Component
@Data
@Slf4j
public class NsfocusTask {
    @Autowired
    private SnmpService snmpService;

    @Async("taskExecutor")
    public void colletcTask(String ip, int port, String community,String neid){

        NsfocusClient client = new NsfocusClient(ip, port, community);
        List<Nestatus> datas = new ArrayList<>(16);

        Nestatus nestatus = client.collectCPU();
        nestatus.setNeid(neid);
        nestatus.setLastrefreshtime(new Timestamp(System.currentTimeMillis()));
        nestatus.setRecordid(UUID.randomUUID().toString());
        datas.add(nestatus);

        nestatus = client.collectMemory();
        nestatus.setNeid(neid);
        nestatus.setLastrefreshtime(new Timestamp(System.currentTimeMillis()));
        nestatus.setRecordid(UUID.randomUUID().toString());
        datas.add(nestatus);

        snmpService.saveSnmpData(datas);



    }

}
