package com.cic.inventory.repositories.specifications;

import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.types.AssetStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AssetSpecifications {

    private AssetSpecifications() {
    }

    public static Specification<Asset> build(
            String locationName,
            String departmentName,
            String search,
            String status,
            String category,
            String supplierName
    ) {
        return (root, query, cb) -> {
            // Eagerly join the to-one associations the response mapper reads
            // for every row (toResponse() touches location/supplier/assignedTo)
            // so a page of results doesn't trigger N+1 lazy-load queries.
            // Safe with pagination since these are all @ManyToOne (single-valued)
            // — no row multiplication like a collection fetch would cause.
            // Skipped on the count query Spring Data runs to compute totalElements.
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("location", JoinType.LEFT);
                root.fetch("supplier", JoinType.LEFT);
                root.fetch("assignedTo", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(locationName)) {
                predicates.add(cb.equal(cb.lower(root.get("location").get("name")), locationName.toLowerCase(Locale.ROOT)));
            }

            if (StringUtils.hasText(departmentName)) {
                predicates.add(cb.equal(
                        cb.lower(root.get("assignedTo").get("department").get("name")),
                        departmentName.toLowerCase(Locale.ROOT)
                ));
            }

            if (StringUtils.hasText(search)) {
                String like = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("assetCode")), like),
                        cb.like(cb.lower(root.get("brand")), like),
                        cb.like(cb.lower(root.get("model")), like),
                        cb.like(cb.lower(root.get("serialNo")), like)
                ));
            }

            if (StringUtils.hasText(status)) {
                try {
                    predicates.add(cb.equal(root.get("status"), AssetStatus.valueOf(status.trim().toUpperCase(Locale.ROOT))));
                } catch (IllegalArgumentException ignored) {
                    // unknown status value — ignore the filter rather than failing the whole query
                }
            }

            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase(Locale.ROOT)));
            }

            if (StringUtils.hasText(supplierName)) {
                predicates.add(cb.equal(cb.lower(root.get("supplier").get("name")), supplierName.trim().toLowerCase(Locale.ROOT)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
