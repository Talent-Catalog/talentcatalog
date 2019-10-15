export interface LanguageLevelFormControlModel {
  languageId?: number;
  spokenLevelId?: number;
  writtenLevelId?: number;
}

export const emptyLanguageLevelFormControlModel = {
  languageId: null,
  spokenLevelId: null,
  writtenLevelId: null
};
