package com.example.application.service;

import com.example.application.common.MultiResponse;
import com.example.application.dto.query.Skill2QueryRequest;
import com.example.domain.skill2.model.Skill2Filter;
import com.example.domain.skill2.repository.Skill2Projection;
import com.example.domain.skill2.repository.Skill2QueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Style 2 应用服务 —— 读操作直调研仓库（CQRS），不经过领域服务。
 */
@Service
public class Skill2AppService {

    private final Skill2QueryRepository queryRepository;

    public Skill2AppService(Skill2QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Transactional(readOnly = true)
    public MultiResponse<Skill2Projection> search(Skill2QueryRequest req) {
        Skill2Filter filter = Skill2Filter.builder()
                .keyword(req.getKeyword())
                .category(req.getCategory())
                .level(req.getLevel())
                .status(req.getStatus())
                .build();

        int pageIndex = Math.max(1, req.getPageIndex());
        int pageSize = Math.max(1, Math.min(req.getPageSize(), 100));
        int offset = (pageIndex - 1) * pageSize;

        List<Skill2Projection> list = queryRepository.searchByFilter(filter, offset, pageSize);
        long total = queryRepository.countByFilter(filter);
        return MultiResponse.of(list, total, pageIndex, pageSize);
    }
}
