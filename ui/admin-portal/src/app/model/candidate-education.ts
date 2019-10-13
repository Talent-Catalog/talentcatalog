import {Country} from "./country";

export interface CandidateEducation {
  id: number;
  educationType: string;
  country: Country;
  lengthOfCourseYears: number;
  institution: string;
  courseName: string;
  yearCompleted: string;
};
