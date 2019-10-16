export interface LanguageLevelFormControlModel {
  languageId?: number;
  spokenLevel?: number;
  writtenLevel?: number;
}

export const emptyLanguageLevelFormControlModel = {
  languageId: null,
  spokenLevel: null,
  writtenLevel: null
};
