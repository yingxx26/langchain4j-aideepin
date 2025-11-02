package com.moyz.adi.common.workflow.node.sqlexecutor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SqlExecutorNodeConfig {

    @NotBlank
    private String sqlListStr;

    /*private List<Param> headers;
    private List<Param> params;

    @JsonProperty("content_type")
    private String contentType;
    @JsonProperty("text_body")
    private String textBody;
    @JsonProperty("form_data_body")
    private List<Param> formDataBody;
    @JsonProperty("form_urlencoded_body")
    private List<Param> formUrlencodedBody;
    @JsonProperty("json_body")
    private JsonNode jsonBody;

    private Integer timeout;

    @JsonProperty("retry_times")
    private Integer retryTimes;
    @JsonProperty("clear_html")
    private Boolean clearHtml;

    @Data
    public static class Param {
        private String name;
        private Object value;
    }*/

}
