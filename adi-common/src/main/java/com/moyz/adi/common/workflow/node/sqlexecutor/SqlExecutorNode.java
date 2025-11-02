package com.moyz.adi.common.workflow.node.sqlexecutor;

import com.aliyun.core.utils.StringUtils;
import com.moyz.adi.common.entity.WorkflowComponent;
import com.moyz.adi.common.entity.WorkflowNode;
import com.moyz.adi.common.util.JsonUtil;
import com.moyz.adi.common.util.SchemaAnalyzer;
import com.moyz.adi.common.util.SqlExutor;
import com.moyz.adi.common.workflow.NodeProcessResult;
import com.moyz.adi.common.workflow.WfNodeState;
import com.moyz.adi.common.workflow.WfState;
import com.moyz.adi.common.workflow.data.NodeIOData;
import com.moyz.adi.common.workflow.node.AbstractWfNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.moyz.adi.common.cosntant.AdiConstant.FORM_DATA_BOUNDARY_PRE;
import static com.moyz.adi.common.cosntant.AdiConstant.WorkflowConstant.DEFAULT_OUTPUT_PARAM_NAME;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
public class SqlExecutorNode extends AbstractWfNode {

    public SqlExecutorNode(WorkflowComponent wfComponent, WorkflowNode node, WfState wfState, WfNodeState nodeState) {
        super(wfComponent, node, wfState, nodeState);
    }

    protected NodeProcessResult onProcess() {
        List<NodeIOData> outputData = new ArrayList<>();

        SqlExecutorNodeConfig nodeConfig = checkAndGetConfig(SqlExecutorNodeConfig.class);
        String sqlListStr = nodeConfig.getSqlListStr();
 
        //执行sql
        List<String> sqls = SchemaAnalyzer.extractSql2List(sqlListStr);
        StringBuilder sqlresult = new StringBuilder();
        for (String sql : sqls) {
            System.out.println("=======sql========" + sql.toString());
            sqlresult = sqlresult.append(SqlExutor.executeQueryToJson(sql));
        }

        NodeIOData output = NodeIOData.createByText(DEFAULT_OUTPUT_PARAM_NAME, "响应内容", sqlListStr);
        outputData.add(output);
        return NodeProcessResult.builder().content(outputData).build();
    }

}
