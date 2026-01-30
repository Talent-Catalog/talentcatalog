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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JacksonException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.tctalent.server.configuration.SystemAdminConfiguration;
import org.tctalent.server.repository.db.read.dao.CandidateReadDao;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.service.db.SavedListService;

/**
 * These tests should always be run to check that CandidateReadDto and its referenced classes are
 * up to date with the database schema.
 *
 * @author John Cameron
 */
@Tag("skip-test-in-gradle-build")
@SpringBootTest
class CandidateReadDaoTest {

    @Autowired
    private CandidateReadDao candidateReadDao;

    @Autowired
    SavedListService savedListService;

    private Set<Long> testCandidateIds;

    @BeforeEach
    void setUp() {
        //Tests are run on whatever candidates are in standard TestCandidates list.
        testCandidateIds = savedListService.fetchCandidateIds(SystemAdminConfiguration.TEST_CANDIDATE_LIST_ID);
        assertFalse(testCandidateIds.isEmpty());
    }

    @Test
    void findByIds() {
        assertNotNull(candidateReadDao);

        final List<CandidateReadDto> dtos;
        try {
            dtos = candidateReadDao.findByIds(testCandidateIds);
            assertNotNull(dtos);
        } catch (BadSqlGrammarException ex) {
            String message = """
            
            
            Grammatical error in the SQL query generated from CandidateReadDto.
            
            This is usually because there is a field in CandidateReadDto or one of the DTO classes
            that it contains that does not map to a field in the database.
            
            Perhaps there is a typo in the field name or the database field mapping in a DTO is
            not correct.
            
            """;
            SQLException causedBy = ex.getSQLException();
            if (causedBy != null) {
                message += """
                    Cause given in exception:
                    """ + causedBy.getMessage();
            }
            fail(message);
        } catch (UncategorizedSQLException ex) {
            String message = """
            
            
            Uncategorized error in the SQL query generated from CandidateReadDto.
            
            This usually occurs when there is a problem converting a field retrieved from the
            database into its matching field in the relevant Java DTO.
            See CandidateReadMapper code for details.
            
            Normally the problem will be a type mismatch where the data in the database cannot be
            converted into the Java type expected by the DTO.
            
            """;
            SQLException causedBy = ex.getSQLException();
            if (causedBy != null) {
                Throwable nestedCause = causedBy.getCause();
                if (nestedCause instanceof JacksonException) {
                    message += """
                        Cause given in exception:
                        """ + nestedCause.getMessage();
                } else {
                    message += """
                        Cause given in exception:
                        """ + causedBy.getMessage();
                }
            }
            fail(message);
        }

    }
}
