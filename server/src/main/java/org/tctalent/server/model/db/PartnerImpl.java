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

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.util.SalesforceHelper;

@Getter
@Setter
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

    private boolean defaultJobCreator;

    private boolean defaultSourcePartner;

    /**
     * Optional link to employer associated with partner - only used for employer partners.
     * <p/>
     * Note that link is One to One. In other words there can only be one partner
     * associated with a given employer.
     */
    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private Employer employer;

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
            if (registrationLandingPage.trim().isEmpty()) {
                registrationLandingPage = null;
            }
        }
        this.registrationLandingPage = registrationLandingPage;
    }

    /**
     * Simple toString rather than Lombok generated one which was causing an infinite recursion
     * error.
     * @return Partner name
     */
    @Override
    public String toString() {
        return name;
    }
}
