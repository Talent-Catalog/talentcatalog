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

package org.tctalent.server.model.db;

import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.tctalent.server.service.db.ExportColumnsService;

/**
 * Base class for sources for candidates.
 * Sub classes include SavedSearches and SavedLists.
 * <p/>
 * Common base class functionality includes:
 * <ul>
 *     <li>Names</li>
 *     <li>Status - active, inactive, deleted</li>
 *     <li>Fixed attribute - not modifiable except by owner</li>
 *     <li>WatcherIds - Other users can be notified about changes </li>
 *     <li>Supporting sharing with other users</li>
 *     <li>Non default displayed candidate fields</li>
 * </ul>
 *
 * @author John Cameron
 */
@MappedSuperclass
public abstract class AbstractCandidateSource extends AbstractAuditableDomainObject<Long>
    implements HasPublicId {

    /**
     * The name given to the candidate source
     */
    private String name;

    private String publicId;

    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * Description of the purpose of this candidate source
     */
    @Nullable
    private String description;

    /**
     * If not null (or empty) this is the list of candidate fields which are
     * displayed for each candidate.
     * <p/>
     * Each candidate field is defined as the name of the candidate attribute,
     * or if a candidate attribute is a reference to another entity, then
     * the name of the candidate attribute followed by "." then the name of the
     * attribute of the nested entity.
     * For example, "updatedDate" for the candidate's updated date field,
     * or "user.firstName" for the candidate's first name (the "firstName"
     * attribute of the candidate's "user" attribute).
     * <p/>
     * If null or empty, a default set of fields are displayed (as defined
     * in the Angular front end code).
     * <p/>
     * The code allows for two different ways of displaying candidates of
     * a candidate source - a "long" one, and a more compact "short" one.
     */
    @Convert(converter = CommaDelimitedStringsConverter.class)
    @Nullable
    private List<String> displayedFieldsLong;

    /**
     * @see #displayedFieldsLong
     */
    @Convert(converter = CommaDelimitedStringsConverter.class)
    @Nullable
    private List<String> displayedFieldsShort;

    /**
     * If true, only the owner can modify the details of the candidate source
     */
    private Boolean fixed = false;

    /**
     * If true, all users will see this candidate source. It does not need to be
     * shared.
     */
    private Boolean global = false;

    /**
     * Salesforce EmployerJob opportunity, if one exists, associated
     * with this source of candidates.
     */
    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private SalesforceJobOpp sfJobOpp;

    /**
     * Stored as comma separated list of watching user ids
     */
    private String watcherIds;

    protected AbstractCandidateSource() {
        setStatus(Status.active);
    }

    /**
     * If not null this is defines which columns are exported for each candidate when publishing
     * candidates from this candidate source.
     * <p/>
     * Note that the actual definition of each column is managed in the front end Angular code.
     * The server only stores keys identifying the columns and any extra configuration.
     * <p/>
     * SavedList or SavedSearch implementations have slightly different ways of storing
     * export columns on the database - so the actual database mapping has to be done in those
     * subclasses.
     * @return List of columns. Note that the ordering is defined by the
     * {@link ExportColumn#getIndex()} attribute in each ExportColumn.
     */
    @Nullable
    public abstract List<ExportColumn> getExportColumns();

    /**
     * @see #getExportColumns()
     * <p/>
     * Note: You can't call setExportColumns and assuming that previous columns on the database
     * are deleted. You need to explicitly do that using
     * {@link ExportColumnsService}
     */
    public abstract void setExportColumns(@Nullable List<ExportColumn> exportColumns);

    protected void modifyColumnIndices(@Nullable List<ExportColumn> exportColumns) {
        if (exportColumns != null) {
            int index = 0;
            for (ExportColumn exportColumn : exportColumns) {
                exportColumn.setIndex(index++);
            }
        }
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public List<String> getDisplayedFieldsLong() {
        return displayedFieldsLong;
    }

    public void setDisplayedFieldsLong(@Nullable List<String> displayedFieldsLong) {
        this.displayedFieldsLong = displayedFieldsLong;
    }

    @Nullable
    public List<String> getDisplayedFieldsShort() {
        return displayedFieldsShort;
    }

    public void setDisplayedFieldsShort(@Nullable List<String> displayedFieldsShort) {
        this.displayedFieldsShort = displayedFieldsShort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFixed() {
        return fixed;
    }

    public void setFixed(Boolean fixed) {
        if (fixed != null) {
            this.fixed = fixed;
        }
    }

    public Boolean getGlobal() {
        return global;
    }

    public void setGlobal(Boolean global) {
        if (global != null) {
            this.global = global;
        }
    }

    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    @Nullable
    public SalesforceJobOpp getSfJobOpp() {
        return sfJobOpp;
    }

    public void setSfJobOpp(@Nullable SalesforceJobOpp sfJobOpp) {
        this.sfJobOpp = sfJobOpp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    //Support for watchers

    public String getWatcherIds() {
        return watcherIds;
    }

    public void setWatcherIds(@Nullable String watcherIds) {
        this.watcherIds = watcherIds;
    }

    /**
     * Extracts List of watching user ids from watcherIds String/
     * @return Set of user ids. Empty List if no watchers.
     */
    @NotNull
    public Set<Long> getWatcherUserIds() {
        return watcherIds == null ? new HashSet<>() :
                Stream.of(watcherIds.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
    }

    /**
     * Encodes the given list of watching user ids as a comma delimited String
     * and sets the watcherIds field to that.
     * @param watcherUserIds List of watching user ids
     */
    public void setWatcherUserIds(@Nullable Set<Long> watcherUserIds) {
        final String s = CollectionUtils.isEmpty(watcherUserIds) ? null :
                watcherUserIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
        setWatcherIds(s);
    }

    /**
     * Adds a new watching user
     * @param userId User id of new watcher
     */
    public void addWatcher(Long userId) {
        Set<Long> ids = getWatcherUserIds();
        ids.add(userId);
        setWatcherUserIds(ids);
    }

    /**
     * Removes a watching user
     * @param userId User id of watcher to be removed
     */
    public void removeWatcher(Long userId) {
        Set<Long> ids = getWatcherUserIds();
        ids.remove(userId);
        setWatcherUserIds(ids);
    }


    //Support for sharing

    /**
     * Clears all users
     */
    private void clearUsers() {
        Set<User> users = getUsers();

        //First update each user object to no longer refer to this candidate source
        for (User user : users) {
            getUsersCollection(user).remove(this);
        }

        //Finally clear the collection of users
        users.clear();
    }

    /**
     * This is the collection of users who share this candidate source.
     * It will return the field on the entity corresponding to a many to many
     * mapping in the database of user ids to the ids of these candidate sources.
     * @return Set of users
     */
    public abstract Set<User> getUsers();

    /**
     * Set the collection of users who share this candidate source.
     * It removes any previous users in the collection.
     * @param users Set of users who share this candidate source.
     */
    public void setUsers(@Nullable Set<User> users) {
        //Clear existing collection of users and add new ones.
        clearUsers();
        if (users != null) {
            for (User user : users) {
                addUser(user);
            }
        }
    }

    /**
     * Return the given user's collection of shared instances of this class of
     * candidate source.
     * <p/>
     * This is the user side of the many to many sharing relationship.
     * <p/>
     * Typically the implementing code will be a single line like:
     * <code>
     *     user.[getSharesCollection()];
     * </code>
     * where [getSharesCollection] is the User method which returns the
     * collection of candidate source instances the user is sharing.
     *
     * @param user User who can share this class of candidate source
     * @param <T> A subclass of this class - eg a Saved Search
     * @return Given user's collection of shared instances
     */
    public abstract <T extends AbstractCandidateSource> Set<T> getUsersCollection(User user);

    /**
     * Share this instance with given user
     * @param user User to share with
     */
    public void addUser(User user) {
        //Add user to the collection of users sharing this candidate source
        getUsers().add(user);
        //Also update other side of many to many relationship, adding this
        //instance to the user's collection of shared instances.
        getUsersCollection(user).add(this);
    }

    /**
     * Stop sharing this instance with given user
     * @param user User to stop sharing with
     */
    public void removeUser(User user) {
        //Remove user from the collection of users sharing this candidate source
        getUsers().remove(user);
        //Also update other side of many to many relationship, removing this
        //instance from the user's collection of shared instances.
        getUsersCollection(user).remove(this);
    }

}
