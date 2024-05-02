import {Employer} from "../../../model/partner";

class MockEmployer implements Employer {
  id: number = 1;
  name: string = "Mock Employer";
  description: string = "Mock employer description";
  hasHiredInternationally: boolean = true;
  sfId: string = "mock_sf_id";
  website: string = "https://example.com";

  constructor(employerData?: Partial<Employer>) {
    if (employerData) {
      Object.assign(this, employerData);
    }
  }
}

export const mockEmployer: MockEmployer = new MockEmployer();
