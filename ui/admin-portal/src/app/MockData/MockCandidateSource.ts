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

import {MockUser} from "./MockUser";
import {User} from "../model/user";
import {Auditable, CandidateSource} from "../model/base";
import {ExportColumn, PublishedDocColumnProps, SavedList} from "../model/saved-list";
import {OpportunityIds} from "../model/opportunity";
import {Task} from "../model/task";

export class MockCandidateSource implements CandidateSource,SavedList {
  name: string = "Mock Candidate";
  description?: string = "Mock description";
  displayedFieldsLong?: string[] = ["Field1", "Field2"];
  displayedFieldsShort?: string[] = ["ShortField1", "ShortField2"];
  exportColumns?: ExportColumn[] = [new ExportColumn()];
  fixed: boolean = false;
  global: boolean = true;
  sfJobOpp?: OpportunityIds = { sfId: "mockSfId" };
  users?: User[] = [new MockUser()];
  watcherUserIds?: number[] = [1];
  id: number = 1;
  createdBy?: User = new MockUser();
  createdDate?: Date = new Date("2024-05-01");
  updatedBy?: User = new MockUser();
  updatedDate?: Date = new Date("2024-05-01");
  tasks?: Task[] = []; // Add the tasks property
}

export class MockExportColumn implements ExportColumn {
  key: string = "mockKey";
  properties?: PublishedDocColumnProps = { header: "Mock Header", constant: "Mock Constant" };
}

export class MockPublishedDocColumnProps implements PublishedDocColumnProps {
  header: string = "Mock Header";
  constant: string = "Mock Constant";
}

export class MockOpportunityIds implements OpportunityIds {
  sfId?: string = "mockSfId";
  id: number = 1;
}

export class MockAuditable implements Auditable {
  id: number = 1;
  createdBy?: User = new MockUser();
  createdDate?: Date = new Date("2024-05-01");
  updatedBy?: User = new MockUser();
  updatedDate?: Date = new Date("2024-05-01");
}
