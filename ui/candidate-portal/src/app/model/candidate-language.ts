import {Language} from "./language";

export interface CandidateLanguage {
  id: number;
  language: Language;
  speak: string;
  readWrite: string;
}
