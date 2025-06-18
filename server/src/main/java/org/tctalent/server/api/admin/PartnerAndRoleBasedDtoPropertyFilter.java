/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.api.admin;

import java.util.HashSet;
import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.util.dto.DtoPropertyFilter;

/**
 * Filters out a candidate's properties in a DtoBuilder based on a user's role and associated partner.
 * <p/>
 * Broadly, a user can only see public properties of candidates who do not belong to the user's
 * partner. The one exception is users with role systemadmin - they can always see everything.
 * If a candidate does belong to the user's partner, then the properties can be viewed depend on
 * the role of that user.
 *
 * @author John Cameron
 */
public class PartnerAndRoleBasedDtoPropertyFilter implements DtoPropertyFilter {

    @Nullable private Set<Candidate> fullyVisibleCandidates;
    @Nullable private Set<User> fullyVisibleUsers; //Computed from fullyVisibleCandidates
    @Nullable
    private final Partner partner;

    @NonNull
    private final Role role;

    @Nullable
    private final Set<String> publicProperties;

    @Nullable
    private final Set<String> semiLimitedExtraProperties;

    /**
     * Filters out non-public Candidate properties according the given parameters
     * @param partner Partner associated with the user requesting the candidate data.
     * @param role Role of user requesting the candidate data
     * @param fullyVisibleCandidates If present, this specifies the candidates that we can return
     *                               all data for (ie no filtering)
     * @param publicProperties Names of properties that are always returned (ie never filtered out)
     * @param semiLimitedExtraProperties Names of properties that may be filtered out.
     */
    public PartnerAndRoleBasedDtoPropertyFilter(@Nullable Partner partner, @NonNull Role role,
        @Nullable Set<Candidate> fullyVisibleCandidates,
        @Nullable Set<String> publicProperties, @Nullable Set<String> semiLimitedExtraProperties) {
        this.partner = partner;
        this.role = role;
        this.publicProperties = publicProperties;
        this.semiLimitedExtraProperties = semiLimitedExtraProperties;

        setFullyVisibleCandidates(fullyVisibleCandidates);
    }

    @Override
    public boolean ignoreProperty(Object o, String property) {
        boolean ignore;

        if (role == Role.systemadmin
            || isDefaultPartner(partner) && (role == Role.admin || role == Role.partneradmin)
            || isInFullyVisibleCandidates(o)
        ) {
            // System admins, default partner (TBB) admins, fully visible candidates (see method) can see all fields.
            ignore = false;
        } else {
            if (publicProperties != null && publicProperties.contains(property)) {
                //Public properties are never ignored
                ignore = false;
            } else {
                //It is not a public property - so could be ignored. Depends on partner type and role.
                //TODO JC This code needs to be modified to understand recruiter ownership
                Partner candidatePartner = fetchPartner(o);
                if (partner != null && candidatePartner != null) {
                    ignore = roleBasedFilter(role, partner, candidatePartner, property);
                } else {
                    // If partner or candidate partner is null, then only show public properties
                    ignore = true;
                }
            }
        }
        return ignore;
    }

    private boolean roleBasedFilter(Role role, Partner partner, Partner candidatePartner, String property) {
        boolean ignore;
        switch (role) {
            case admin:
            case partneradmin:
                // Source partner admins can only see full details if the candidate is assigned to their partner
                if (partner.isSourcePartner()) {
                    ignore = isNotPartnerMatch(partner, candidatePartner);
                } else if (isViewerPartner(partner)) {
                    // Viewer partners can only see the semi limited (no personal details) fields, regardless of role
                    ignore = !isVisibleToViewerPartner(property);
                } else {
                    // All other admins can see all data (e.g. destination partners, recruiter partners, TBB parter)
                    ignore = false;
                }
                break;
            case limited:
                // Limited roles can only see public properties
                ignore = true;
                break;
            case semilimited:
                // Semi limited roles can see some additional properties to the public properties if they exist
                ignore = semiLimitedExtraProperties == null
                        || !semiLimitedExtraProperties.contains(property);
                // However, source partner semi limited users can't see the extra semi limited properties if they aren't
                // assigned to the same partner as the candidate.
                if (partner.isSourcePartner() && isNotPartnerMatch(partner, candidatePartner)) {
                    ignore = true;
                }
               break;
            default:
                ignore = true;
        }
        return ignore;
    }

    /**
     * Checks if an admin portal user partner matches a candidate's assigned partner
     * @param partner admin portal user partner
     * @param candidatePartner candidate's assigned partner
     * @return boolean if match or not
     */
    private boolean isNotPartnerMatch(Partner partner, Partner candidatePartner) {
        return !partner.getId().equals(candidatePartner.getId());
    }

    /**
     * A unique partner type that isn't source or destination, they are only to view candidate information that is not
     * identifiable as they don't need to view personal details. E.g. UNHCR
     * @param partner the partner of the user trying to view candidate/s
     * @return boolean
     */
    private boolean isViewerPartner(Partner partner) {
        return !partner.isSourcePartner() && !partner.isJobCreator() && !isDefaultPartner(partner);
    }

    /**
     * Viewer Partners shouldn't see identifiable details regardless of their role - so need to only return the
     * semi limited properties for these partners.
     * @param property property to check if it's ignored
     * @return boolean of ignore value
     */
    private boolean isVisibleToViewerPartner(String property) {
        return semiLimitedExtraProperties != null
                && !semiLimitedExtraProperties.isEmpty()
                && semiLimitedExtraProperties.contains(property);
    }

    private boolean isDefaultPartner(Partner partner) {
        boolean res = false;
        if (partner != null) {
            if (partner.isSourcePartner()) {
                res = partner.isDefaultSourcePartner();
            } else if (partner.isJobCreator()) {
                res = partner.isDefaultJobCreator();
            }
        }
        return res;
    }

    private boolean isInFullyVisibleCandidates(Object o) {
        boolean inFullyVisibleCandidates = false;
            if (o instanceof Candidate) {
                inFullyVisibleCandidates =
                    fullyVisibleCandidates != null && fullyVisibleCandidates.contains(o);
            } else if (o instanceof User) {
                inFullyVisibleCandidates =
                    fullyVisibleUsers != null && fullyVisibleUsers.contains(o);
            }
        return inFullyVisibleCandidates;
    }

    /**
     * Fetches the source partner associated with the given object
     * @param o Object - should be a Candidate or a User
     * @return Partner associated with object, or null if none found
     */
    @Nullable
    private Partner fetchPartner(Object o) {
        User user = null;
        if (o instanceof Candidate) {
            user = ((Candidate) o).getUser();
        } else if (o instanceof User) {
            user = (User) o;
        }

        return user == null ? null : user.getPartner();
    }

    private void setFullyVisibleCandidates(@Nullable Set<Candidate> fullyVisibleCandidates) {
        this.fullyVisibleCandidates = fullyVisibleCandidates;

        //Construct matching fullyVisibleUsers from the users associated with each fully visible
        //candidate.
        if (fullyVisibleCandidates == null) {
            fullyVisibleUsers = null;
        } else {
            fullyVisibleUsers = new HashSet<>();
            for (Candidate candidate : fullyVisibleCandidates) {
                fullyVisibleUsers.add(candidate.getUser());
            }
        }
    }
}

