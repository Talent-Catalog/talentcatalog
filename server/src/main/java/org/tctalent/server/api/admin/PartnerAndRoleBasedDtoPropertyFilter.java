/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
            //Allows default partner (eg TBB) admins to see everything.
            || isDefaultPartner(partner) && (role == Role.admin || role == Role.partneradmin)
            || isInFullyVisibleCandidates(o)
        ) {
            ignore = false;
        } else {
            if (publicProperties != null && publicProperties.contains(property)) {
                //Public properties are never ignored
                ignore = false;
            } else {
                //It is not a public property - so could be ignored.

                //TODO JC This code needs to be modified to understand recruiter ownership

                //It is ignored if the candidate's partner do not match the given partner, or either
                //partner is null.
                Partner candidatePartner = fetchPartner(o);
                if (partner == null || candidatePartner == null ||
                    !partner.getId().equals(candidatePartner.getId())) {
                    ignore = true;
                } else {
                    //Partner matches.
                    //Whether it is ignored depends on the user's role.
                    switch (role) {

                        case admin:
                        case partneradmin:
                            //Admins see all candidate properties
                            ignore = false;
                            break;

                        case limited:
                            //Limited roles can only see public properties
                            ignore = true;
                            break;

                        case semilimited:
                            //Ignore if property is not one of the extra semilimited properties
                            ignore = semiLimitedExtraProperties == null
                                || !semiLimitedExtraProperties.contains(property);
                            break;

                        default:
                            //To be safe, ignore if null or unexpected new roles
                            ignore = true;
                    }
                }
            }
        }
        return ignore;
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

