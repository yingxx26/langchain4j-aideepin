package com.moyz.adi.common.workflow.node.dbtableinfo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DbTableInfoNodeConfig {


    private String dburl;


    private String dbuser;


    private String dbpassword;


    private String dbtable;

}
