import {MockUser} from "./MockUser";
import {User} from "../model/user";
import {Auditable, CandidateSource} from "../model/base";
import {ExportColumn, PublishedDocColumnProps} from "../model/saved-list";
import {OpportunityIds} from "../model/opportunity";

export class MockCandidateSource implements CandidateSource {
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
