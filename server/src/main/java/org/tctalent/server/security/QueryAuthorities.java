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

    boolean hasAuthority(String authority);

    boolean hasRole(String role);

    boolean hasAnyAuthority(String... authorities);

    boolean hasAnyRole(String... roles);

}
