// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.security;

/**
 * Interface for checking authorities
 *
 * @author John Cameron
 */
public interface QueryAuthorities {

    /**
     * True if the given authority is present.
     */
    boolean hasAuthority(String authority);

    /**
     * True if the given role is present.
     */
    boolean hasRole(String role);

    /**
     * True if any of the given authorities are present.
     */
    boolean hasAnyAuthority(String... authorities);

    /**
     * True if any of the given roles are present.
     */
    boolean hasAnyRole(String... roles);

}
