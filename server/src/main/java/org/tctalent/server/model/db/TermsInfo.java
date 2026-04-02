
/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.model.db;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * "Terms" are legal terms. For example privacy policies. These terms are typically displayed to
 * users requesting their acceptance.
 * <p>
 * Terms are classified by {@link #type}.
 * <p>
 * Each Terms instance has a unique String id which can be anything but normally is a readable
 * string constructed from the Terms {@link #type} and a version number eg "V2".
 * So for example, "GrnCandidatePolicyV2" might be the id for Version 2 of terms of type
 * {@link TermsType#GRN_CANDIDATE_PRIVACY_POLICY}.
 * <p>
 * There can be multiple versions of the same type of terms. Each version will have a different
 * {@link #createdDate} and {@link #id} and {@link #pathToContent}.
 * <p>
 * So Users can be linked to the specific version of a type of terms that they have consented to.
 * <p>
 *     For example, the current candidate privacy policy
 *     (type = {@link TermsType#GRN_CANDIDATE_PRIVACY_POLICY}) might have id = "GrnCandidatePolicyV2".
 *     And the content of the policy is found at path "/terms/GrnGDPRPrivacyPolicy-20260312.html".
 * <p>
 *     The previous policy was id="GrnCandidatePolicyV1" pointing to a different path.
 * <p>
 *     Those two policy instances will be stored in two TermInfos.
 * <p>
 *     Each candidate has an acceptedPrivacyPolicyId which might be "GrnCandidatePolicyV1" or
 *     "GrnCandidatePolicyV2". Using that id, the corresponding TermsInfo can be fetched.
 *     It will have the path to the policy text and also the type of the policy and when the
 *     policy was created. By looking at other TermInfo's of the same type, you can figure
 *     out which is the latest policy.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class TermsInfo {

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
    private LocalDate createdDate;

    /**
     * Unique id for these terms.
     */
    private String id;

    /**
     * Resource path to file containing {@link #content}.
     * <p/>
     * For example: "/terms/GrnGDPRPrivacyPolicy-20250604.html"
     */
    private String pathToContent;

    /**
     * The type of terms - for example a privacy policy.
     */
    @Enumerated(EnumType.STRING)
    private TermsType type;

    public TermsInfo(String id, String pathToContent, TermsType type, LocalDate createdDate) {
        this.id = id;
        this.pathToContent = pathToContent;
        this.type = type;
        this.createdDate = createdDate;
    }
}
