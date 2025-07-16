package com.cpsoneghett.codingtask.repository.impl;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceFilter;
import com.cpsoneghett.codingtask.repository.DeviceRepositoryQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DeviceRepositoryQueryImpl implements DeviceRepositoryQuery {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Device> filter(DeviceFilter filter, Pageable pageable) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Device> criteria = builder.createQuery(Device.class);
        Root<Device> root = criteria.from(Device.class);

        Predicate[] predicates = createRestrictions(filter, builder, root);
        criteria.where(predicates);

        TypedQuery<Device> query = em.createQuery(criteria);
        addPagingRestrictions(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(filter));
    }

    private Predicate[] createRestrictions(DeviceFilter filter, CriteriaBuilder builder, Root<Device> root) {

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(filter.brand())) {
            predicates.add(builder.like(builder.lower(root.get("brand")), "%" + filter.brand() + "%"));
        }

        if (StringUtils.hasText(filter.state())) {
            predicates.add(builder.like(builder.lower(root.get("state")), "%" + filter.brand() + "%"));
        }

        return predicates.toArray(new Predicate[0]);
    }

    private void addPagingRestrictions(TypedQuery<Device> query, Pageable pageable) {
        int currentPage = pageable.getPageNumber();
        int totalPerPage = pageable.getPageSize();
        int firstPageRegister = currentPage * totalPerPage;

        query.setFirstResult(firstPageRegister);
        query.setMaxResults(totalPerPage);
    }

    private Long total(DeviceFilter filter) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Device> root = criteria.from(Device.class);

        Predicate[] predicates = createRestrictions(filter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));

        return em.createQuery(criteria).getSingleResult();
    }
}
