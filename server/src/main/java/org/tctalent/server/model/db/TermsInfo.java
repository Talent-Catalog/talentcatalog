
/*
 * Copyright (c) 2025 Talent Catalog.
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

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * "Terms" are legal terms. For example privacy policies. These terms are typically displayed to
 * users requesting their acceptance.
 * <p/>
 * Terms are classified by {@link #type}.
 * <p/>
 * There can be multiple versions of the same type of terms. Each version will have a different
 * {@link #createdDate}.
 * <p/>
 * Users can be linked to the specific version of a type of terms that they have consented to.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "terms_info")
@SequenceGenerator(name = "seq_gen", sequenceName = "terms_info_id_seq", allocationSize = 1)
public class TermsInfo extends AbstractDomainObject<Long> {

    /**
     * HTML content of the terms.
     */
    private String content;

    /**
     * Time when these terms were created.
     * <p/>
     * There can be multiple versions of the same terms type.
     * The "current" version of that type of terms will be the one with the most recent createdDate.
     */
    private OffsetDateTime createdDate;

    /**
     * The type of terms - for example a privacy policy.
     */
    @Enumerated(EnumType.STRING)
    private TermsType type;

    /**
     * The version this type of policy.
     */
    private String version;
}
