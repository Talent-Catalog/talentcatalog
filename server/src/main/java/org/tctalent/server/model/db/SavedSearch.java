/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.tctalent.server.logging.LogBuilder;

@Entity
@Table(name = "saved_search")
@SequenceGenerator(name = "seq_gen", sequenceName = "saved_search_id_seq", allocationSize = 1)
@Getter
@Setter
@Slf4j
public class SavedSearch extends AbstractCandidateSource {

    private String type;

    /**
     * Every user has one default search. It is opened every time they use the 'New Search' tab
     * and overwritten with any different new search they initiate. In effect, this means a user
     * opening the 'New Search' tab will always see their most recently initiated new search.
     */
    private Boolean defaultSearch = false;

    /**
     * This is the query string that will be used to search text related to candidates.
     * Currently, it is based on Elasticsearch style simple query string syntax.
     */
    private String simpleQueryString;

    private String keyword;
    private String statuses;
    private Gender gender;

    private String occupationIds;
    private Integer minYrs;
    private Integer maxYrs;

    private String partnerIds;

    private String candidateNumbers;

    private String listAllIds;
    @Enumerated(EnumType.STRING)
    private SearchType listAllSearchType;

    private String listAnyIds;
    @Enumerated(EnumType.STRING)
    private SearchType listAnySearchType;

    private String nationalityIds;
    @Enumerated(EnumType.STRING)
    private SearchType nationalitySearchType;

    private String countryIds;
    @Enumerated(EnumType.STRING)
    private SearchType countrySearchType;

    private String surveyTypeIds;

    private Integer englishMinWrittenLevel;
    private Integer englishMinSpokenLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exclusion_list_id")
    @Nullable
    private SavedList exclusionList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "other_language_id")
    private Language otherLanguage;
    private Integer otherMinWrittenLevel;
    private Integer otherMinSpokenLevel;

    private LocalDate lastModifiedFrom;

    private LocalDate lastModifiedTo;

    private LocalDate createdFrom;
    private LocalDate createdTo;

    private Integer minAge;
    private Integer maxAge;


    private Integer minEducationLevel;
    private String educationMajorIds;

    private Boolean includePendingTermsCandidates;
    private Boolean miniIntakeCompleted;
    private Boolean fullIntakeCompleted;
    private Boolean potentialDuplicate;

    private String regoReferrerParam;

    private String unhcrStatuses;

    /**
     * If specified, requests display of candidates who have any candidate opportunities
     * (anyOpps = true) or who have no candidate opportunities (anyOpps = false)
     */
    private Boolean anyOpps;

    /**
     * If specified, requests display of candidates who have any closed candidate opportunities
     * (closedOpps = true) or who have any open candidate opportunities (closedOpps = false)
     */
    private Boolean closedOpps;

    /**
     * If specified, requests display of candidates who have any candidate opportunities whose stage
     * is relocated or greater (relocatedOpps = true) or who have any candidate opportunities
     * whose stage is less than relocated (relocatedOpps = false)
     */
    private Boolean relocatedOpps;

    /**
     * Reviewable searches allow the front end to supply review filters to the search
     * in the form of a List of ReviewStatus's. Using the review filters engages the
     * CandidateReviewItem table to decide which candidates should be excluded from the
     * results of the search (ie candidates whose review status for the search does not match
     * one of the statuses in the provided review filter).
     * <p/>
     * When a search is marked as not reviewable, the front end will not supply review filters.
     */
    private Boolean reviewable = false;

    //TODO JC There is only ever one "SearchJoin" per search - this is legacy code where each search
    //could be based on a boolean expression of base searches. Too complex and was dropped ages ago.
    //Should be replace with a single base search - and no further need for the SearchJoin class or
    // entity
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "savedSearch", cascade = CascadeType.MERGE)
    private Set<SearchJoin> searchJoins = new HashSet<>();

    //Note use of Set rather than List as strongly recommended for Many to Many
    //relationships here:
    // https://thoughts-on-java.org/best-practices-for-many-to-many-associations-with-hibernate-and-jpa/
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "sharedSearches", cascade = CascadeType.MERGE)
    private Set<User> users = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "savedSearch", cascade = CascadeType.MERGE)
    @OrderBy("id")
    private List<ExportColumn> exportColumns;

    @Transient private List<String> countryNames;
    @Transient private List<String> partnerNames;
    @Transient private List<String> nationalityNames;
    @Transient private List<String> surveyTypeNames;
    @Transient private List<String> vettedOccupationNames;
    @Transient private List<String> occupationNames;
    @Transient private List<String> educationMajors;
    @Transient private String englishWrittenLevel;
    @Transient private String englishSpokenLevel;
    @Transient private String otherWrittenLevel;
    @Transient private String otherSpokenLevel;
    @Transient private String minEducationLevelName;
    @Transient private SavedSearchType savedSearchType;
    @Transient private SavedSearchSubtype savedSearchSubtype;

    public SavedSearch() {
    }

    @Nullable
    public List<ExportColumn> getExportColumns() {
        return exportColumns;
    }

    public void setExportColumns(@Nullable List<ExportColumn> exportColumns) {
        modifyColumnIndices(exportColumns);
        this.exportColumns = exportColumns;
    }


    public void setDefaultSearch(Boolean defaultSearch) {
        if (defaultSearch != null) {
            this.defaultSearch = defaultSearch;
        }
    }


    @Nullable
    public SavedList getExclusionList() {
        return exclusionList;
    }

    public void setExclusionList(@Nullable SavedList exclusionList) {
        this.exclusionList = exclusionList;
    }


    public void setType(String type) {
        this.type = type;
    }

    public void setType(SavedSearchType savedSearchType, SavedSearchSubtype subtype) {
        setSavedSearchType(savedSearchType);
        setSavedSearchSubtype(subtype);

        String type = makeStringSavedSearchType(savedSearchType, subtype);
        setType(type);
    }

    public static String makeStringSavedSearchType(
            SavedSearchType savedSearchType, SavedSearchSubtype subtype) {
        String type = null;
        if (savedSearchType != null) {
            type = savedSearchType.toString();
            if (subtype != null) {
                type += "/" + subtype.toString();
            }
        }
        return type;
    }


    @Nullable public SavedSearchSubtype getSavedSearchSubtype() {
        return savedSearchSubtype;
    }

    public void setSavedSearchSubtype(@Nullable SavedSearchSubtype savedSearchSubtype) {
        this.savedSearchSubtype = savedSearchSubtype;
    }

    public void parseType() {
        if (!StringUtils.isEmpty(type)) {
            String[] parts = type.split("/");
            try {
                SavedSearchType savedSearchType = SavedSearchType.valueOf(parts[0]);
                setSavedSearchType(savedSearchType);

                //Check for subtype
                if (parts.length > 1) {
                    SavedSearchSubtype savedSearchSubtype = SavedSearchSubtype.valueOf(parts[1]);
                    setSavedSearchSubtype(savedSearchSubtype);
                }
            } catch (IllegalArgumentException ex) {
                LogBuilder.builder(log)
                    .action("SavedSearchType")
                    .message("Bad type '" + type + "' of saved search " + getId())
                    .logError(ex);
            }
        }
    }


    public void setReviewable(Boolean reviewable) {
        if (reviewable != null) {
            this.reviewable = reviewable;
        }
    }

    @Override
    public Set<SavedSearch> getUsersCollection(User user) {
        return user.getSharedSearches();
    }
}
