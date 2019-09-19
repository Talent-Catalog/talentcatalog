import {Country} from "./country";

export interface WorkExperience {
  id: number;
  country: Country;
  companyName: string;
  role: string;
  startDate: string;
  endDate: string;
  fullTime: string;
  paid: string;
  description: string;
}
