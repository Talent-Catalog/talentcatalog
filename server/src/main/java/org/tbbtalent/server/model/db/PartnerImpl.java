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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.util.SalesforceHelper;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity(name = "Partner")
@Table(name = "partner")
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

    @Column(name="default_destination_partner")
    private boolean defaultJobCreator;

    private boolean defaultSourcePartner;

    /**
     * True if this partner is a job creator - ie the partner can have users who can create jobs
     */
    private boolean jobCreator;

    @Nullable
    private String logo;

    @NonNull
    private String name;

    @Nullable
    private String notificationEmail;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "partner", cascade = CascadeType.MERGE)
    private Set<PartnerJobRelation> partnerJobRelations = new HashSet<>();

    @Nullable
    public String getSfId() {
        return SalesforceHelper.extractIdFromSfUrl(sflink);
    }

    @Nullable
    private String sflink;

    /**
     * True if this partner is a source partner
     */
    private boolean sourcePartner;

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


    //Source Partner fields

    private boolean defaultPartnerRef;

    @Nullable
    private String registrationLandingPage;

    @ManyToMany
    @JoinTable(
            name = "partner_source_country",
            joinColumns = @JoinColumn(name = "partner_id"),
            inverseJoinColumns = @JoinColumn(name = "country_id"))
    @NonNull
    private Set<Country> sourceCountries = new HashSet<>();

    private boolean autoAssignable;

    public void setRegistrationLandingPage(@Nullable String registrationLandingPage) {
        if (registrationLandingPage != null) {
            if (registrationLandingPage.trim().length() == 0) {
                registrationLandingPage = null;
            }
        }
        this.registrationLandingPage = registrationLandingPage;
    }

}
