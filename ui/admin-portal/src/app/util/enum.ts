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
  return Object.keys(enumeration)
    .map(key => ({ value: key, displayText: enumeration[key] }));
}


