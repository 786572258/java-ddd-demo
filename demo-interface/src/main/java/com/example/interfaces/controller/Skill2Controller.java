package com.example.interfaces.controller;

import com.example.application.common.MultiResponse;
import com.example.application.dto.query.Skill2QueryRequest;
import com.example.application.service.Skill2AppService;
import com.example.domain.skill2.repository.Skill2Projection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Style 2 接口 —— CQRS 读直接走 QueryRepository，不经过领域服务。
 */
@RestController
@RequestMapping("/api/skill2")
public class Skill2Controller {

    private final Skill2AppService skill2AppService;

    public Skill2Controller(Skill2AppService skill2AppService) {
        this.skill2AppService = skill2AppService;
    }

    @GetMapping
    public MultiResponse<Skill2Projection> search(Skill2QueryRequest req) {
        return skill2AppService.search(req);
    }
}
