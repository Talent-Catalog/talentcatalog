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
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.partner.SourcePartner;

@Getter
@Setter
@ToString
@Entity(name = "SourcePartner")
@DiscriminatorValue("SourcePartner")
public class SourcePartnerImpl extends PartnerImpl
    implements SourcePartner {

    @Override
    public String getPartnerType() {
        return "SourcePartner";
    }

    @Nullable
    private String registrationLandingPage;

    @Nullable
    private String registrationUrl;

    //TODO JC Note - unidirectional - no mapping from Country
    //TODO JC Eager necessary? Test.
    @ManyToMany
    @JoinTable(
        name = "partner_source_country",
        joinColumns = @JoinColumn(name = "partner_id"),
        inverseJoinColumns = @JoinColumn(name = "country_id"))
    @NonNull
    private Set<Country> sourceCountries = new HashSet<>();

    private boolean defaultSourcePartner;

}
