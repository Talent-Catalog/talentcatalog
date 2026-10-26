/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.repo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.JobChatRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.JobChatUserService;

/**
 * Hibernate persistence behaviour our code relies on, on the real database: saving entities with
 * assigned (composite) ids - insert, then update - which Spring Data does with merge, updating
 * detached entities, dirty checking, and lazy loading inside and outside a transaction.
 * <p>
 * Added in TC-1189 (Hibernate 6.6 to 7.4, which changed some merge rules) because these paths
 * were otherwise only tested with mocks.
 * </p>
 */
@SpringBootTest
class PersistenceBehaviourIntegrationTest extends BaseDBIntegrationTest {

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository userRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private JobChatRepository jobChatRepository;
    @Autowired private CandidatePropertyService candidatePropertyService;
    @Autowired private JobChatUserService jobChatUserService;

    private TransactionTemplate tx;
    private Long userId;
    private Long candidateId;
    private Long chatId;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            String name = "tc1189_persist_" + UUID.randomUUID();
            User user = TestDataFactory.createUser(null);
            user.setUsername(name);
            user.setEmail(name + "@example.com");
            user = userRepository.save(user);
            userId = user.getId();

            Candidate candidate = new Candidate();
            candidate.setUser(user);
            candidate.setCandidateNumber("TC1189P" + (Math.abs(name.hashCode()) % 1_000_000));
            candidate.setStatus(CandidateStatus.active);
            candidate.setPhone("+61 400 000 000");
            candidate.setWhatsapp("+61 400 000 001");
            candidate.setContactConsentRegistration(true);
            candidate.setContactConsentPartners(true);
            candidate.setUnhcrStatus(UnhcrStatus.Unsure);
            candidate.setGender(Gender.male);
            candidate.setCreatedBy(user);
            candidate = candidateRepository.save(candidate);
            candidateId = candidate.getId();

            JobChat chat = new JobChat();
            chat.setType(JobChatType.CandidateProspect);
            chat.setCandidate(candidate);
            chat.setCreatedBy(user);
            chat.setCreatedDate(OffsetDateTime.now());
            chatId = jobChatRepository.save(chat).getId();
        });
    }

    @AfterEach
    void tearDown() {
        jdbc.update("delete from job_chat_user where job_chat_id = ?", chatId);
        jdbc.update("delete from job_chat where id = ?", chatId);
        jdbc.update("delete from candidate_property where candidate_id = ?", candidateId);
        jdbc.update("delete from candidate where id = ?", candidateId);
        jdbc.update("delete from users where id = ?", userId);
    }

    @Test
    @DisplayName("an entity with an assigned composite id is inserted, then updated")
    void assignedCompositeIdInsertThenUpdate() {
        tx.executeWithoutResult(status -> candidatePropertyService.createOrUpdateProperty(
            candidateRepository.findById(candidateId).orElseThrow(), "tc1189Prop", "first", null));
        tx.executeWithoutResult(status -> candidatePropertyService.createOrUpdateProperty(
            candidateRepository.findById(candidateId).orElseThrow(), "tc1189Prop", "second", null));

        assertThat(jdbc.queryForList(
            "select value from candidate_property where candidate_id = ? and name = 'tc1189Prop'",
            String.class, candidateId)).containsExactly("second");
    }

    @Test
    @DisplayName("marking a chat as read inserts, then updates, its job chat user row")
    void jobChatUserInsertThenUpdate() {
        for (int i = 0; i < 2; i++) {
            tx.executeWithoutResult(status -> jobChatUserService.markChatAsRead(
                jobChatRepository.findById(chatId).orElseThrow(),
                userRepository.findById(userId).orElseThrow(), null));
        }

        assertThat(jdbc.queryForObject(
            "select count(*) from job_chat_user where job_chat_id = ? and user_id = ?",
            Integer.class, chatId, userId)).isEqualTo(1);
    }

    @Test
    @DisplayName("a detached entity is updated by saving it, and a managed entity by dirty checking")
    void detachedSaveAndDirtyChecking() {
        Candidate detached = tx.execute(status ->
            candidateRepository.findById(candidateId).orElseThrow());
        detached.setDob(LocalDate.of(1990, 5, 17));
        tx.executeWithoutResult(status -> candidateRepository.save(detached));

        assertThat(jdbc.queryForObject("select dob from candidate where id = ?",
            LocalDate.class, candidateId)).isEqualTo(LocalDate.of(1990, 5, 17));

        //No save call: the change to the managed entity is flushed on commit
        tx.executeWithoutResult(status -> candidateRepository.findById(candidateId).orElseThrow()
            .setWorkAbroadNotes("dirty checked"));

        assertThat(jdbc.queryForObject("select work_abroad_notes from candidate where id = ?",
            String.class, candidateId)).isEqualTo("dirty checked");
    }

    @Test
    @DisplayName("lazy associations load inside a transaction but not after it")
    void lazyLoading() {
        String usernameInTx = tx.execute(status ->
            jobChatRepository.findById(chatId).orElseThrow().getCandidate().getUser().getUsername());
        assertThat(usernameInTx).startsWith("tc1189_persist_");

        JobChat detachedChat = tx.execute(status -> jobChatRepository.findById(chatId).orElseThrow());
        assertThatThrownBy(() -> detachedChat.getCandidate().getUser().getUsername())
            .isInstanceOf(LazyInitializationException.class);
    }
}
