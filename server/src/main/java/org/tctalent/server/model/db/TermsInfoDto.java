// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.model.db;

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

    /**
     * Unique id of terms
     */
    private long id;

    /**
     * HTML content of the terms.
     */
    private String content;
}
