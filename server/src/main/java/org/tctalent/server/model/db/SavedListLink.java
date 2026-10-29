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

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//TODO JC Is this Zombie code - SavedListLink?
@Getter
@Setter
@ToString
@Entity
@Table(name = "saved_list_link")
//With no generator declared, Hibernate 6 used SavedList's saved_list_id_seq, so ids have come from
//that sequence. Hibernate 7 needs it declared explicitly - TC-1189.
@SequenceGenerator(name = "seq_gen", sequenceName = "saved_list_id_seq", allocationSize = 1)
public class SavedListLink extends AbstractAuditableDomainObject<Long>{
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_list_id")
    private SavedList savedList;

    private String link;
}
