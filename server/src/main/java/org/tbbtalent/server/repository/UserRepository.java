package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.User;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /* Used for candidate authentication */
    @Query(" select distinct u from User u "
            + " where lower(u.username) = lower(:username) ")
    User findByUsernameIgnoreCase(@Param("username") String username);

    /* Used for candidate authentication */
    @Query(" select distinct u from User u "
            + " where lower(u.email) = lower(:email) ")
    User findByEmailIgnoreCase(@Param("email") String email);


}
