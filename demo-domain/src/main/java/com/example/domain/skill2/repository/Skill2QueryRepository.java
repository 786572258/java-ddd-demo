package com.example.domain.skill2.repository;

import com.example.domain.skill2.model.Skill2Filter;

import java.util.List;

/**
 * Style 2 CQRS 读仓库 — 只存放复杂列表查询，返回轻量 Projection。
 *
 * <p>对比 {@code com.example.domain.skill.repository.SkillReadRepository}：
 * <ul>
 *   <li>SkillReadRepository: 返回 SkillReadModel(record)，需要 EntityManager 查询 + 手动映射</li>
 *   <li>Skill2QueryRepository: 返回 Projection(interface)，JPA 构造器表达式直接映射</li>
 * </ul>
 */
public interface Skill2QueryRepository {

    List<Skill2Projection> findAllSummary();

    /** 多条件动态筛选，参数包装为领域对象 Skill2Filter */
    List<Skill2Projection> searchByFilter(Skill2Filter filter, int offset, int limit);

    long countByFilter(Skill2Filter filter);
}
