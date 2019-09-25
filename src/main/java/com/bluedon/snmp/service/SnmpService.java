package com.bluedon.snmp.service;

import com.bluedon.snmp.common.Const;
import com.bluedon.snmp.entity.Nestatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author liujh
 * @date 2019/9/24 17:51
 */
@Service
public class SnmpService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getSnmpParams(Map<String, String> params){
        if (params==null||params.size()==0) {
            return null;
        }
        List<Map<String, Object>> datas = jdbcTemplate.queryForList("select ne.ne_id,ip.device_ip,log.port ,log.community  " +
                "from t_siem_moni_ne ne LEFT JOIN t_moni_device_ip ip on ne.ne_id=ip.ne_id " +
                "LEFT JOIN t_moni_ne_login log on ne.ne_id= log.neid where htypeid='"+params.get(Const.TYPEID)+"' " +
                "and vendorid='"+params.get(Const.VENDORID)+"' and ne.status<>2 and ip.status<>2 and log.status<>2");
        return datas==null||datas.size()==0? null :datas;
    }


    public void saveSnmpData(List<Nestatus> datas) {
        if (datas==null||datas.size()==0) {
            return ;
        }
        String neid = datas.get(0).getNeid();
        //先删再存
        jdbcTemplate.execute("delete from t_siem_nestatus where neid='"+neid+"'");

        String insertsql = "INSERT INTO \"public\".\"t_siem_nestatus\"(\"recordid\", \"neid\", \"statustype\", \"statusname\", \"scanname\", \"numvalue\", \"strvalue\", \"lastrefreshtime\", \"remark\") " +
                "VALUES (?, ?, 1, ?, ?, ?, ?, ?, NULL)";
        jdbcTemplate.batchUpdate(insertsql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Nestatus nestatus = datas.get(i);
                ps.setString(1, nestatus.getRecordid());
                ps.setString(2, nestatus.getNeid());
                ps.setString(3, nestatus.getStatusname());
                ps.setString(4, nestatus.getScanname());
                ps.setString(5, nestatus.getNumvalue());
                ps.setString(6, nestatus.getStrvalue());
                ps.setTimestamp(7, nestatus.getLastrefreshtime());
            }

            @Override
            public int getBatchSize() {
                return datas.size();
            }
        });


    }

}
