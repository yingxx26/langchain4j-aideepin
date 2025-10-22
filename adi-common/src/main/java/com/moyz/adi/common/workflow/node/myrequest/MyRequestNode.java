package com.moyz.adi.common.workflow.node.myrequest;

import com.aliyun.core.utils.StringUtils;
import com.moyz.adi.common.entity.WorkflowComponent;
import com.moyz.adi.common.entity.WorkflowNode;
import com.moyz.adi.common.util.JsonUtil;
import com.moyz.adi.common.workflow.NodeProcessResult;
import com.moyz.adi.common.workflow.WfNodeState;
import com.moyz.adi.common.workflow.WfState;
import com.moyz.adi.common.workflow.data.NodeIOData;
import com.moyz.adi.common.workflow.node.AbstractWfNode;
import com.moyz.adi.common.workflow.node.httprequest.HttpRequestNodeConfig;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.moyz.adi.common.cosntant.AdiConstant.FORM_DATA_BOUNDARY_PRE;
import static com.moyz.adi.common.cosntant.AdiConstant.WorkflowConstant.DEFAULT_OUTPUT_PARAM_NAME;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
public class MyRequestNode extends AbstractWfNode {


    public MyRequestNode(WorkflowComponent wfComponent, WorkflowNode node, WfState wfState, WfNodeState nodeState) {
        super(wfComponent, node, wfState, nodeState);
    }

    @Override
    protected NodeProcessResult onProcess() {
        List<NodeIOData> outputData = new ArrayList<>();
        MyRequestNodeConfig nodeConfig = checkAndGetConfig(MyRequestNodeConfig.class);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(nodeConfig.getTimeout() * 1000)
                .setConnectTimeout(nodeConfig.getTimeout() * 1000)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(nodeConfig.getRetryTimes(), true))
                .build();
        String url = appendParams(nodeConfig.getUrl(), nodeConfig.getParams());
        String contentType = nodeConfig.getContentType();
        HttpUriRequest httpRequest;
        if (HttpGet.METHOD_NAME.equalsIgnoreCase(nodeConfig.getMethod())) {
            httpRequest = new HttpGet(url);
            httpRequest.setHeader(CONTENT_TYPE, contentType);
            setHeaders(httpRequest, nodeConfig.getHeaders());
        } else if (HttpPost.METHOD_NAME.equalsIgnoreCase(nodeConfig.getMethod())) {
            HttpPost httpPost = new HttpPost(url);
            httpRequest = httpPost;
            httpRequest.setHeader(CONTENT_TYPE, contentType);
            setHeaders(httpRequest, nodeConfig.getHeaders());
            if (contentType.equalsIgnoreCase("text/plain")) {
                StringEntity stringEntity = new StringEntity(nodeConfig.getTextBody(), ContentType.TEXT_PLAIN.withCharset(Consts.UTF_8));
                httpPost.setEntity(stringEntity);
            } else if (contentType.equalsIgnoreCase("application/json")) {
                StringEntity jsonEntity = new StringEntity(JsonUtil.toJson(nodeConfig.getJsonBody()), ContentType.APPLICATION_JSON.withCharset(Consts.UTF_8));
                httpPost.setEntity(jsonEntity);
            } else if (contentType.equalsIgnoreCase("application/x-www-from-urlencoded")) {
                StringEntity jsonEntity = new StringEntity(JsonUtil.toJson(nodeConfig.getFormUrlencodedBody()), ContentType.APPLICATION_FORM_URLENCODED.withCharset(Consts.UTF_8));
                httpPost.setEntity(jsonEntity);
            } else if (contentType.equalsIgnoreCase("multipart/form-data")) {
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
                if (nodeConfig.getFormDataBody() != null) {
                    for (MyRequestNodeConfig.Param entry : nodeConfig.getFormDataBody()) {
                        if (entry.getValue() instanceof File) {
                            multipartEntityBuilder.addPart(entry.getName(), new FileBody((File) entry.getValue()));
                        } else {
                            multipartEntityBuilder.addTextBody(entry.getName(), entry.getValue().toString());
                        }
                    }
                }
                String boundary = FORM_DATA_BOUNDARY_PRE + System.currentTimeMillis();
                multipartEntityBuilder.setBoundary(boundary);
                httpPost.setEntity(multipartEntityBuilder.build());
                httpRequest.setHeader(CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
            }
        } else {
            log.error("不支持的请求方式:{}", nodeConfig.getMethod());
            throw new RuntimeException();
        }
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = null;
        try {
            responseBody = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Boolean.TRUE.equals(nodeConfig.getClearHtml())) {
            Document doc = Jsoup.parse(responseBody);
            responseBody = doc.body().text();
        }
        if (statusCode == 200) {
            NodeIOData output = NodeIOData.createByText(DEFAULT_OUTPUT_PARAM_NAME, "响应内容", responseBody);
            outputData.add(output);
        } else {
            NodeIOData output = NodeIOData.createByText(DEFAULT_OUTPUT_PARAM_NAME, "错误内容", responseBody);
            outputData.add(output);
        }
        outputData.add(NodeIOData.createByText("status_code", "http状态码", String.valueOf(statusCode)));
        return NodeProcessResult.builder().content(outputData).build();
    }

    private String appendParams(String url, List<MyRequestNodeConfig.Param> params) {

        if (params.isEmpty()) {
            return url;
        } else {
            String result = "";
            Iterator<MyRequestNodeConfig.Param> iterator = params.iterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                MyRequestNodeConfig.Param next = iterator.next();
                String value = next.getValue().toString();
                String name = next.getName();
                stringBuilder.append("&");
                stringBuilder.append(name);
                stringBuilder.append("=");
                stringBuilder.append(value);
            }
            result = stringBuilder.toString();
            if (url.contains("?")) {
                return url + result;
            } else {
                return url + "?" + result;
            }
        }
    }

    private void setHeaders(HttpUriRequest httpRequest, List<MyRequestNodeConfig.Param> headers) {
        for (MyRequestNodeConfig.Param header : headers) {
            httpRequest.addHeader(header.getName(), header.getValue().toString());
        }
    }

}
