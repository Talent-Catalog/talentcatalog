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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
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
    private String publicId;

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
     * True if the partner has public api access.
     * @return True if the partner has public api access.
     */
    public boolean isPublicApiAccess() {
        return publicApiKeyHash != null;
    }

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

    /**
     * Authorities granted to this partner on the public API.
     * <p/>
     * Empty if partner does not have access to the public API
     */
    @Convert(converter = PublicApiAuthorityConverter.class)
    private Set<PublicApiAuthority> publicApiAuthorities = new HashSet<>();

    /**
     * Convert null authorities to empty authorities
     * @param authorities Authorities - can be null
     */
    public void setPublicApiAuthorities(@Nullable Set<PublicApiAuthority> authorities) {
        this.publicApiAuthorities = authorities == null ? new HashSet<>() : authorities;
    }

    /**
     * Public API key used by partner to access the public API.
     * <p/>
     * This is a transient value (ie not stored on the database) which is only populated when
     * an API key is first created. It is sent to the user's browser just once so that it can
     * be copied and sent securely to the partner in question. No copy is kept by TC staff.
     */
    @Transient
    @Nullable
    private String publicApiKey;

    /**
     * Hash of public API key used by partner to access the public API.
     * <p/>
     * Null if partner does not have access to the public API
     */
    @Nullable
    private String publicApiKeyHash;

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

    /**
     * Data Processing Agreement that partner has accepted
     */
    @Nullable
    private String acceptedDataProcessingAgreementId;

    /**
     * Date time when partner accepted data processing agreement
     */
    @Nullable
    private OffsetDateTime acceptedDataProcessingAgreementDate;

    /**
     * First date of Data Processing Agreement that partner has been seen
     */
    @Nullable
    private OffsetDateTime firstDpaSeenDate;

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

    /**
     * If this partner is inactive and a registering candidate uses a URL that would otherwise
     * assign them to it, this field can redirect them to a new one.
     */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redirect_partner_id")
    private PartnerImpl redirectPartner;

    @Override
    public boolean canManageCandidatesInCountry(Country country) {
        if (!isSourcePartner()) {
            return false;
        }

        if (isDefaultSourcePartner()) {
            return true;
        }

        return this.getSourceCountries().contains(country);
    }

}
