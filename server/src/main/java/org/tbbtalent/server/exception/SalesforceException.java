/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.exception;

/**
 * Data related errors coming back from Salesforce
 *
 * @author John Cameron
 */
public class SalesforceException extends ServiceException {

    public SalesforceException(String errors) {
        super("salesforce", errors);
    }
    
}
