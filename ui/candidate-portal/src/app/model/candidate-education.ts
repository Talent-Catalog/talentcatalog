import {Country} from "./country";
import {EducationMajor} from "./education-major";

export interface CandidateEducation {
  id: number;
  educationType: string;
  country: Country;
  educationMajor: EducationMajor;
  lengthOfCourseYears: number;
  institution: string;
  courseName: string;
  yearCompleted: string;
  // Request object variables
  countryId?: number;
  educationMajorId?: number;
}
