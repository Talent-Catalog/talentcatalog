import {Language} from "./language";
import {LanguageLevel} from "./language-level";

export interface CandidateLanguage {
  id: number;
  language?: Language;
  spokenLevel?: LanguageLevel;
  writtenLevel?: LanguageLevel;
  // Request object variables
  languageId?: number;
  spokenLevelId?: number;
  writtenLevelId?: number;
}
