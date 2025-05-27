// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

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
