/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.service.db.impl.SalesforceServiceImpl;

@Getter
@Setter
@ToString
@Entity
@Table(name = "partner")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "partner_type")
@DiscriminatorValue("Partner")
public abstract class PartnerImpl extends AbstractDomainObject<Long>
    implements Partner {

    private Long id;

    @Nullable
    private String abbreviation;

    @Nullable
    private String logo;

    @NonNull
    private String name;

    @Nullable
    private String notificationEmail;

    abstract public String getPartnerType();

    @Nullable
    public String getSfId() {
        return SalesforceServiceImpl.extractIdFromSfUrl(sflink);
    }

    @Nullable
    private String sflink;

    @Enumerated(EnumType.STRING)
    @NonNull
    private Status status;

    @Nullable
    private String websiteUrl;
}
