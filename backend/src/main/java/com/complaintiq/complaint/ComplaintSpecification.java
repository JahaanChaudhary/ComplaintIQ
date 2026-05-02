package com.complaintiq.complaint;
import com.complaintiq.complaint.enums.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.*;
public class ComplaintSpecification {
    private ComplaintSpecification() {}
    public static Specification<Complaint> withFilters(ComplaintStatus status, UrgencyLevel urgency, ComplaintCategory category, Long agentId, Long customerId, LocalDate dateFrom, LocalDate dateTo, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (urgency != null) predicates.add(cb.equal(root.get("urgency"), urgency));
            if (category != null) { Join<Object,Object> aiJoin = root.join("aiAnalysis", JoinType.LEFT); predicates.add(cb.equal(aiJoin.get("category"), category)); }
            if (agentId != null) { Join<Object,Object> assignJoin = root.join("assignment", JoinType.LEFT); predicates.add(cb.equal(assignJoin.get("agent").get("id"), agentId)); }
            if (customerId != null) predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay()));
            if (dateTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo.plusDays(1).atStartOfDay()));
            if (keyword != null && !keyword.isBlank()) { String pattern = "%" + keyword.toLowerCase() + "%"; predicates.add(cb.or(cb.like(cb.lower(root.get("title")), pattern), cb.like(cb.lower(root.get("description")), pattern), cb.like(cb.lower(root.get("ticketId")), pattern))); }
            assert query != null; query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
