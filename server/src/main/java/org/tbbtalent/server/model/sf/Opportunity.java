/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.sf;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a Salesforce Opportunity.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class Opportunity {
    public String Name;
    public String AccountId;
}
