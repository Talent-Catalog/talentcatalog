import {Country} from './country';
import {Candidate, FamilyRelations, YesNoUnsure} from './candidate';

export interface CandidateDestination {
  id?: number;
  country?: Country;
  candidate?: Candidate;
  interest?: YesNoUnsure;
  family?: FamilyRelations;
  location?: string;
  notes?: string;
};
