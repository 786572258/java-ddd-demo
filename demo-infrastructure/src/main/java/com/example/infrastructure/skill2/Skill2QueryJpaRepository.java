package com.example.infrastructure.skill2;

import com.example.domain.skill2.model.Skill2Filter;
import com.example.domain.skill2.repository.Skill2Projection;
import com.example.domain.skill2.repository.Skill2QueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * CQRS 读仓库实现 —— 用 JPQL 构造器表达式直接映射到投影接口。
 *
 * <p>和风格一 {@code SkillReadRepositoryImpl} 的对比：
 * <ul>
 *   <li>风格一：EntityManager.createQuery → 返回 List&lt;SkillPO&gt;
 *       → stream().map(SkillReadConverter::toReadModel) → List&lt;SkillReadModel&gt;。</li>
 *   <li>风格二：EntityManager.createQuery → JPQL {@code SELECT new Skill2ProjectionImpl(...)}
 *       → 一句映射，不需要中间步骤。</li>
 * </ul>
 *
 * <p><b>JPQL 构造器表达式原理：</b>
 * {@code SELECT new 全限定类名(字段1, 字段2, ...) FROM Entity}
 * JPA 会为每一行调用那个类的构造器，直接产出投影对象。
 * 不会触发完整的 Entity 加载（不读不需要的列），性能更好。
 */
@Repository
public class Skill2QueryJpaRepository implements Skill2QueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Skill2Projection> findAllSummary() {
        // JPQL 构造器表达式 —— 只查 4 个字段，不加载完整 Entity
        return cast(em.createQuery("""
                SELECT new com.example.infrastructure.skill2.Skill2ProjectionImpl(s.id, s.name, s.category, s.status)
                FROM Skill2Entity s ORDER BY s.id DESC
                """, Skill2ProjectionImpl.class).getResultList());
    }

    @Override
    public List<Skill2Projection> searchByFilter(Skill2Filter filter, int offset, int limit) {
        var pair = buildWhere(filter);
        TypedQuery<Skill2ProjectionImpl> query = em.createQuery("""
                SELECT new com.example.infrastructure.skill2.Skill2ProjectionImpl(s.id, s.name, s.category, s.status)
                FROM Skill2Entity s
                """ + pair.whereClause + " ORDER BY s.id DESC", Skill2ProjectionImpl.class);
        for (int i = 0; i < pair.params.size(); i++) {
            query.setParameter("p" + i, pair.params.get(i));
        }
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return cast(query.getResultList());
    }

    @Override
    public long countByFilter(Skill2Filter filter) {
        var pair = buildWhere(filter);
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(s) FROM Skill2Entity s" + pair.whereClause, Long.class);
        for (int i = 0; i < pair.params.size(); i++) {
            query.setParameter("p" + i, pair.params.get(i));
        }
        return query.getSingleResult();
    }

    /**
     * 从领域过滤条件动态组装 WHERE 子句。
     * 每个条件只在有值时拼接，避免空条件污染 SQL。
     *
     * <p>这块逻辑放在 infra 层，领域层只传递 Skill2Filter，
     * 不知道 SQL 长什么样。
     */
    private WherePair buildWhere(Skill2Filter f) {
        List<String> clauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (f.keyword() != null && !f.keyword().isBlank()) {
            clauses.add("(s.name LIKE :p" + params.size() + " OR s.description LIKE :p" + params.size() + ")");
            params.add("%" + f.keyword() + "%");
        }
        if (f.category() != null && !f.category().isBlank()) {
            clauses.add("s.category = :p" + params.size());
            params.add(f.category());
        }
        if (f.level() != null && !f.level().isBlank()) {
            clauses.add("s.level = :p" + params.size());
            params.add(f.level());
        }
        if (f.status() != null && !f.status().isBlank()) {
            clauses.add("s.status = :p" + params.size());
            params.add(f.status());
        }

        String whereClause = clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
        return new WherePair(whereClause, params);
    }

    private record WherePair(String whereClause, List<Object> params) {}

    @SuppressWarnings("unchecked")
    private static <T> List<T> cast(List<?> list) {
        return (List<T>) list;
    }
}
