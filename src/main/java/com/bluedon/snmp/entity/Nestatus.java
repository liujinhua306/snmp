package com.bluedon.snmp.entity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author liujh
 * @date 2019/9/25 9:29
 */
@Data
public class Nestatus implements Serializable {
    private String recordid ;

    private String neid ;

    private int statustype = 1;

    private String statusname;

    private String scanname;

    private String numvalue;

    private String strvalue;

    private Timestamp lastrefreshtime;

    private String remark;


}
