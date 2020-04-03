import {Candidate} from "./candidate";
import {SurveyType} from "./survey-type";

export interface CandidateSurvey {
  id: number;
  surveyType: SurveyType;
  candidate: Candidate;
  comment: string;
}
