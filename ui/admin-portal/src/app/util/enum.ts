/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

/**
 * Represents the value of String enum
 * (https://www.typescriptlang.org/docs/handbook/enums.html#string-enums)
 * as a simple structure displaying the "key" or "name" of the value, and its associated string.
 * For example, consider the following enum.
       export enum CandidateStatus {
           draft = "draft (inactive)",
           :
        }
 * For the "draft" value of the above enumeration, the EnumOption.value would be "draft"
 * and the EnumOption.displayText would be "draft (inactive)".
 * <p/>
 * (Note that if it was a numeric enum EnumOption.displayText will be numeric value associated with
 * that enum value).
 */
export interface EnumOption {
  /**
   * This is the actual enumeration value name (also known as key).
   */
  value: string;

  /**
   * This is the string value associated with a "string enumeration".
   */
  displayText: string
}

/**
 * Takes a string enumeration (https://www.typescriptlang.org/docs/handbook/enums.html#string-enums)
 * and returns a corresponding array of values and
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


