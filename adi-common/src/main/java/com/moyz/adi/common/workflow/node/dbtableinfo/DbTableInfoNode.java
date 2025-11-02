package com.moyz.adi.common.workflow.node.dbtableinfo;

import com.moyz.adi.common.entity.WorkflowComponent;
import com.moyz.adi.common.entity.WorkflowNode;
import com.moyz.adi.common.util.SchemaAnalyzer;
import com.moyz.adi.common.util.SqlExutor;
import com.moyz.adi.common.workflow.NodeProcessResult;
import com.moyz.adi.common.workflow.WfNodeState;
import com.moyz.adi.common.workflow.WfState;
import com.moyz.adi.common.workflow.data.NodeIOData;
import com.moyz.adi.common.workflow.node.AbstractWfNode;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static com.moyz.adi.common.cosntant.AdiConstant.WorkflowConstant.DEFAULT_OUTPUT_PARAM_NAME;

@Slf4j
public class DbTableInfoNode extends AbstractWfNode {

    public DbTableInfoNode(WorkflowComponent wfComponent, WorkflowNode node, WfState wfState, WfNodeState nodeState) {
        super(wfComponent, node, wfState, nodeState);
    }

    protected NodeProcessResult onProcess() {
        List<NodeIOData> outputData = new ArrayList<>();

        DbTableInfoNodeConfig nodeConfig = checkAndGetConfig(DbTableInfoNodeConfig.class);
        String dburl = nodeConfig.getDburl();
        String dbuser = nodeConfig.getDbuser();
        String dbpassword = nodeConfig.getDbpassword();
        String dbtable = nodeConfig.getDbtable();

        Connection initialConnection = SchemaAnalyzer.getInitialConnection();
        String schemaDescription = SchemaAnalyzer.getSchemaDescription(initialConnection, "trade_record_zl");


        NodeIOData output = NodeIOData.createByText(DEFAULT_OUTPUT_PARAM_NAME, "响应内容", schemaDescription);
        outputData.add(output);
        return NodeProcessResult.builder().content(outputData).build();
    }

}
