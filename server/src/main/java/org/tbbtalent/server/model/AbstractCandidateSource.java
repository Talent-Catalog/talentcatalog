package org.tbbtalent.server.model;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.springframework.util.CollectionUtils;

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
 * </ul>
 *
 * @author John Cameron
 */
@MappedSuperclass
public abstract class AbstractCandidateSource extends AbstractAuditableDomainObject<Long> {

    /**
     * The name given to the candidate source
     */
    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * If true, only the owner can modify the details of the candidate source
     */
    private Boolean fixed;

    /**
     * Stored as comma separated list of watching user ids 
     */
    private String watcherIds;

    protected AbstractCandidateSource() {
        setStatus(Status.active);
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
        this.fixed = fixed;
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

    public void setWatcherIds(String watcherIds) {
        this.watcherIds = watcherIds;
    }

    /**
     * Extracts List of watching user ids from watcherIds String/
     * @return List of user ids. Empty List if no watchers.
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
    public void setWatcherUserIds(Set<Long> watcherUserIds) {
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
     * This is the collection of users who share this candidate source.
     * It will return the field on the entity corresponding to a many to many
     * mapping in the database of user ids to the ids of these candidate sources. 
     * @return Set of users
     */
    public abstract Set<User> getUsers();

    /**
     * This is the collection of users who share this candidate source.
     * It will return the field on the entity corresponding to a many to many
     * mapping in the database of user ids to the ids of these candidate sources.
     * @param users Set of users who share this candidate source.
     */
    public void setUsers(Set<User> users) {
        //Clear existing collection of users and add new ones.
        getUsers().clear();
        for (User user : users) {
            addUser(user);
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
