import {VisaPathway} from "../services/visa-pathway.service";
import {Job} from "./job";

export interface JobOppIntake {
  employerCostCommitment?: string;
  recruitmentProcess?: string;
  minSalary?: number;
  occupationCode?: string;
  salaryRange?: string;
  locationDetails?: string;
  location?: string;
  visaPathways?: VisaPathway[];
  benefits?: string;
  educationRequirements?: string;
  languageRequirements?: string;
  employmentExperience?: string;
  skillRequirements?: string;
}
