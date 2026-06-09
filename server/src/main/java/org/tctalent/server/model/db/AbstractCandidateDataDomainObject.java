/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.OffsetDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Audited base class for candidate-data entities.
 * <p>
 * The {@link EntityListeners} declaration wires {@link AuditingEntityListener} into the JPA
 * lifecycle for all subclasses, so Spring Data auditing runs on persist/update callbacks.
 * For fields annotated with {@link CreatedBy}, {@link CreatedDate}, {@link LastModifiedBy},
 * and {@link LastModifiedDate}, the listener populates values automatically.
 * <p>
 * The auditing infrastructure is enabled by {@link org.tctalent.server.configuration.JpaAuditingConfig}
 * and uses the configured {@code auditorProvider} ({@link org.tctalent.server.security.SpringSecurityAuditorAware})
 * to resolve the current {@link User} for {@code createdBy}/{@code updatedBy}.
 *
 * @author sadatmalik
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractCandidateDataDomainObject<IdType extends Serializable>
    extends AbstractAuditableDomainObject<IdType> {

    protected AbstractCandidateDataDomainObject() {
        super();
    }

    protected AbstractCandidateDataDomainObject(User createdBy) {
        super(createdBy);
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @CreatedBy
    public User getCreatedBy() {
        return super.getCreatedBy();
    }

    @Override
    @Column(name = "created_date")
    @CreatedDate
    public OffsetDateTime getCreatedDate() {
        return super.getCreatedDate();
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @LastModifiedBy
    public User getUpdatedBy() {
        return super.getUpdatedBy();
    }

    @Override
    @Column(name = "updated_date")
    @LastModifiedDate
    public OffsetDateTime getUpdatedDate() {
        return super.getUpdatedDate();
    }
}
