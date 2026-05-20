// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.security;

import lombok.Getter;
import lombok.Setter;

/**
 * User profile managed by OAuth2 Authentication Provider.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class AuthProfile {
    private String email;
    private String firstName;
    private String idpIssuer;
    private String idpSubject;
    private String lastName;
}
