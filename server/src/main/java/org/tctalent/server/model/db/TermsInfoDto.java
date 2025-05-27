// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.model.db;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * todo document this dto
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class TermsInfoDto {

    private long id;

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
    private TermsType type;

    /**
     * The version this type of policy.
     */
    private String version;
}
