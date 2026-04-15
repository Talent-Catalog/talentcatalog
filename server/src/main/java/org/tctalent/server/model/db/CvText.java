// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.model.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Extracted text of a CV
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class CvText {

    /**
     * Id of CV attachment
     */
    private long id;

    /**
     * Text extracted from CV
     */
    private String text;
}
