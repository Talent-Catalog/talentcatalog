/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PartnerTableDefinitionTest {

  @Test
  void returnsBaseTableSqlStatementsWhenIndexAndFilterExist() {
    PartnerTableDefinition definition = new PartnerTableDefinition(
        "status = 'active'",
        "candidate",
        "id bigint, name varchar(255), status varchar(50)",
        "SELECT id, name, status FROM candidate WHERE deleted = false",
        "id"
    );

    assertEquals("candidate", definition.getTableName());

    assertEquals(
        "CREATE TABLE IF NOT EXISTS candidate(id bigint, name varchar(255), status varchar(50))",
        definition.getCreateTableSQL()
    );

    assertEquals(
        "DROP TABLE IF EXISTS candidate",
        definition.getDropTableSQL()
    );

    assertEquals(
        "CREATE UNIQUE INDEX candidate_id_uindex ON candidate (id)",
        definition.getCreateTableIndexSQL()
    );

    assertEquals(
        "SELECT id, name, status FROM candidate WHERE deleted = false AND status = 'active'",
        definition.getPopulateTableSQL()
    );
  }

  @Test
  void returnsNullIndexSqlAndUnfilteredPopulateSqlWhenIndexAndFilterAreNull() {
    PartnerTableDefinition definition = new PartnerTableDefinition(
        null,
        "partner",
        "id bigint, name varchar(255)",
        "SELECT id, name FROM partner WHERE deleted = false",
        null
    );

    assertNull(definition.getCreateTableIndexSQL());
    assertNull(definition.getCreateTableIndexSQLAsNew());

    assertEquals(
        "SELECT id, name FROM partner WHERE deleted = false",
        definition.getPopulateTableSQL()
    );
  }

  @Test
  void returnsInsertSqlForOneAndMultipleColumns() {
    PartnerTableDefinition definition = new PartnerTableDefinition(
        null,
        "candidate",
        "id bigint, name varchar(255), status varchar(50)",
        "SELECT id, name, status FROM candidate",
        "id"
    );

    assertEquals(
        "INSERT INTO candidate VALUES(?)",
        definition.getInsertSQL(1)
    );

    assertEquals(
        "INSERT INTO candidate VALUES(?,?,?)",
        definition.getInsertSQL(3)
    );
  }

  @Test
  void returnsNewTableSqlStatements() {
    PartnerTableDefinition definition = new PartnerTableDefinition(
        null,
        "candidate",
        "id bigint, name varchar(255)",
        "SELECT id, name FROM candidate",
        "id"
    );

    assertEquals("_new_candidate", definition.getNewTableName());

    assertEquals(
        "DROP TABLE IF EXISTS _new_candidate",
        definition.getDropTableSQLAsNew()
    );

    assertEquals(
        "CREATE TABLE _new_candidate(id bigint, name varchar(255))",
        definition.getCreateTableSQLAsNew()
    );

    assertEquals(
        "CREATE UNIQUE INDEX candidate_id_uindex ON _new_candidate (id)",
        definition.getCreateTableIndexSQLAsNew()
    );

    assertEquals(
        "INSERT INTO _new_candidate VALUES(?)",
        definition.getInsertSQLAsNew(1)
    );

    assertEquals(
        "INSERT INTO _new_candidate VALUES(?,?,?,?)",
        definition.getInsertSQLAsNew(4)
    );
  }

  @Test
  void returnsOldTableAndRenameSqlStatements() {
    PartnerTableDefinition definition = new PartnerTableDefinition(
        null,
        "candidate",
        "id bigint, name varchar(255)",
        "SELECT id, name FROM candidate",
        "id"
    );

    assertEquals(
        "DROP TABLE IF EXISTS _old_candidate",
        definition.getDropTableSQLAsOld()
    );

    assertEquals(
        "RENAME TABLE candidate TO _old_candidate, _new_candidate TO candidate",
        definition.getRenameSQL()
    );
  }
}