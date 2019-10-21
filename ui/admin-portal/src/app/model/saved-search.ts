import {Language} from "./language";

export interface SavedSearch {
  id: number;
  name: string;
  keyword: string;
  gender: string;
  statuses: string;
  occupationIds: number[];
  orProfileKeyword: string;
  verifiedOccupationIds: number[];
  verifiedOccupationSearchType: string;
  nationalityIds: number[];
  nationalitySearchType: string;
  countryIds: number[];
  englishMinWrittenLevel: number;
  englishMinSpokenLevel: number;
  otherLanguage: Language;
  otherMinWrittenLevel: number;
  otherMinSpokenLevel: number;
  lastModifiedFrom: string;
  lastModifiedTo: string;
  createdFrom: string;
  createdTo: string;
  minAge: number;
  maxAge: number;
  minEducationLevel: number;
  educationMajorIds: number[];

  //todo filters
}

export interface SavedSearchJoin {
  savedSearchId: number;
  name: string;
  searchType: 'and' | 'or';
}
