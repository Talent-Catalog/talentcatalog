import {User} from "./user";
import {Partner} from "./partner";

export interface OfferToAssist {
  id: 1,
  createdBy: User,
  createdDate: Date,
  updatedBy: User,
  updatedDate: Date,
  additionalNotes: string,
  partner: Partner,
  publicId: number,
  reason: CandidateAssistanceType
}

export enum CandidateAssistanceType {
  JOB_OPPORTUNITY = "Job Opportunity",
  EDUCATION_SERVICE = "Education Service",
  MIGRATION_SERVICE = "Migration Service",
  FINANCIAL_SUPPORT_SERVICE = "Financial Support Service",
  SETTLEMENT_SERVICE = "Settlement Service",
  OTHER = "Other"
}
