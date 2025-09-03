package com.moyz.adi.common.interfaces;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.*;

import java.util.List;

public interface IStreamingChatAssistant {

    //todo yingxx 核心
    @SystemMessage("{{sm}}")
    TokenStream chatWithSystem(@MemoryId String memoryId, @V("sm") String systemMessage, @UserMessage String prompt, @UserMessage List<ImageContent> images);

    TokenStream chat(@MemoryId String memoryId, @UserMessage String prompt, @UserMessage List<ImageContent> images);

}
