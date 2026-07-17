package com.whatsoeversky.minder.agent.rag;

import com.whatsoeversky.minder.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docs")
public class DocumentController {

    @Resource
    private DocumentIndexService documentIndexService;

    @PostMapping("/reindex")
    public Result<String> reindex() {
        documentIndexService.rebuildIndex();
        return Result.success("知识库索引重建完成");
    }
}
