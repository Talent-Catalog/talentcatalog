/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {IDropdownSettings} from "ng-multiselect-dropdown";

export interface EnumOption {
  value: string;
  displayText: string
}

export const enumMultiSelectSettings: IDropdownSettings = {
  idField: 'value',
  textField: 'displayText',
  enableCheckAll: false,
  singleSelection: false,
  allowSearchFilter: true
};

/**
 * Takes a string enumeration and returns a corresponding array of values and
 * display text suitable for options in an html <select>.
 * @param enumeration String enumeration object
 */
export function enumOptions(enumeration): EnumOption[] {
  return enumKeysToEnumOptions(Object.keys(enumeration), enumeration);
}

/**
 * Takes an array of strings and interprets them as keys of the given string
 * enumeration, returning an  array of values and display text suitable for
 * options in an html <select>.
 * @param keys Array of strings treated as enumeration keys
 * @param enumeration String enumeration object
 */
export function enumKeysToEnumOptions(keys: string[], enumeration): EnumOption[] {
  return keys?.map(key => ({ value: key, displayText: enumeration[key] }));
}


