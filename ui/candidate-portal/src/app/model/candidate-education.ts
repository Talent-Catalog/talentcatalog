import {Country} from "./country";
import {EducationMajor} from "./education-major";

export interface CandidateEducation {
  id: number;
  educationType: string;
  lengthOfCourseYears: number;
  institution: string;
  courseName: string;
  yearCompleted: string;
  country?: Country;
  educationMajor?: EducationMajor;
  // Request object variables
  countryId?: number;
  educationMajorId?: number;
  incomplete?: boolean;
}
