package com.bluedon.snmp.nsfocus.task;

import com.bluedon.snmp.common.Const;
import com.bluedon.snmp.nsfocus.ids.NsfocusClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.bluedon.snmp.service.SnmpService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liujh
 * @date 2019/7/19 9:57
 */
@Component
@Slf4j
public class ColletcNsfocusInfoScheduled {
    @Autowired
    private SnmpService snmpService;
    @Autowired
    private NsfocusTask nsfocusTask;

    @Scheduled(cron = "${task.cron.nsfocus}")
    public void getNsfocusInfo(){
        log.info("开始获取nsfocus的CPU 内存信息");
        Map<String, String> params = new HashMap<>(16);
        params.put(Const.TYPEID, "NIDS");
        params.put(Const.VENDORID, "VNSFOCUS");
        List<Map<String, Object>> snmpParams = snmpService.getSnmpParams(params);
        if (snmpParams!=null&&snmpParams.size()>0) {
            snmpParams.forEach(datas->{
                nsfocusTask.colletcTask(datas.get("device_ip").toString(),Integer.parseInt(datas.get("port").toString()),datas.get("community").toString(),datas.get("ne_id").toString());
            });
        }

    }

    public static void main(String[] args) {
        NsfocusClient client = new NsfocusClient("192.168.1.92", 161, "public");

    }
}
