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

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.util.SalesforceHelper;

@Getter
@Setter
@ToString
@Entity(name = "Partner")
@Table(name = "partner")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "partner_type")
@DiscriminatorValue("Partner")
public class PartnerImpl extends AbstractDomainObject<Long>
    implements Partner {

    private Long id;

    @Nullable
    private String abbreviation;

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_contact_id")
    private User defaultContact;

    @Nullable
    @Transient
    private Long contextJobId;

    @Nullable
    private String logo;

    @NonNull
    private String name;

    @Nullable
    private String notificationEmail;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "partner", cascade = CascadeType.MERGE)
    private Set<PartnerJobRelation> partnerJobRelations = new HashSet<>();

    //See https://stackoverflow.com/questions/43570875/how-to-access-discriminator-column-in-jpa
    @Column(name="partner_type", insertable = false, updatable = false)
    private String partnerType;

    @Nullable
    public String getSfId() {
        return SalesforceHelper.extractIdFromSfUrl(sflink);
    }

    @Nullable
    private String sflink;

    @Enumerated(EnumType.STRING)
    @NonNull
    private Status status;

    @Nullable
    private String websiteUrl;

    public User getJobContact() {
        //User partner contact as default contact.
        User contact = this.defaultContact;
        if (contextJobId != null) {
            for (PartnerJobRelation partnerJob : partnerJobRelations) {
                if (contextJobId.equals(partnerJob.getJob().getId())) {
                    contact = partnerJob.getContact();
                    break;
                }
            }
        }
        return contact;
    }
}
