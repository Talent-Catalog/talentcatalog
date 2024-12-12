/*
 * Copyright (c) 2024 Talent Catalog.
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
 * as a simple structure displaying the "key" of the value, and its associated string value.
 * For example, consider the following enum.
       export enum CandidateStatus {
           draft = "draft (inactive)",
           :
        }
 * For the "draft" value of the above enumeration, the EnumOption.key would be "draft"
 * and the EnumOption.stringValue would be "draft (inactive)".
 * <p/>
 * (Note that if it was a numeric enum, EnumOption.stringValue will be the same as EnumOption.key).
 */
export interface EnumOption {
  /**
   * This is the actual enumeration value key.
   */
  key: string;

  /**
   * This is the string value associated with a "string enumeration". For a normal numeric enum,
   * stringValue will be the same as key.
   */
  stringValue: string
}

/**
 * Takes a string enumeration (https://www.typescriptlang.org/docs/handbook/enums.html#string-enums)
 * and returns a corresponding array of keys and
 * stringValue's suitable for options in an html <select>.
 * @param enumeration String enumeration object
 * @return Array of EnumOptions containing the key and string value associated with each enumeration
 * value
 */
export function enumOptions(enumeration): EnumOption[] {
  return enumKeysToEnumOptions(Object.keys(enumeration), enumeration);
}

/**
 * Takes an array of strings and interprets them as keys of the given string
 * enumeration, returning an array of keys and string values suitable for
 * options in an html <select>.
 * @param keys Array of strings treated as enumeration keys
 * @param enumeration String enumeration object
 * @return Array of EnumOptions containing the key and string value associated with each enumeration
 * value
 */
export function enumKeysToEnumOptions(keys: string[], enumeration): EnumOption[] {
  return keys?.map(key => ({ key: key, stringValue: enumeration[key]}));
}

/**
 * Returns all the keys in the given enumeration object
 * @param enumeration Enumeration type
 */
export function enumKeys(enumeration): string[] {
  return Object.keys(enumeration);
}

/**
 * Returns all the String values in the given enumeration object
 * @param enumeration Enumeration type
 */
export function enumStringValues(enumeration): string[] {
  return Object.keys(enumeration).map(k => enumeration[k]);
}

/**
 * True if the given object is not null or undefined and is an EnumOption
 * @param obj Object to be tested
 */
export function isEnumOption(obj): obj is EnumOption {
  return obj && typeof obj === "object" ? ("key" in obj && "stringValue" in obj) : false;
}

/**
 * True if the given object is not null or undefined and is an array of EnumOption
 * @param obj Object to be tested
 */
export function isEnumOptionArray(obj: Object): obj is EnumOption[] {
  let gotOne: boolean = false;

  //Needs to be an array (note isArray returns false if passed a null or undefined value)
  if (Array.isArray(obj)) {
    //With something in it
    if (obj.length > 0) {
      //Look at first item in array and check its type
      const item = obj[0];
      // When sending ids in multiselect, we know enum option is false if the item is a number.
      if (!isFinite(item)) {
        gotOne = isEnumOption(item);
      }
      //EnumOption objects have a key and a stringValue property.
    }
  }
  return gotOne;
}

/**
 * Fetches the index of a value in an enum. Example is when an enum is ordered, and we want to
 * compare the location of one enum key, in relation to another enum key.
 * @param enumeration this is the Enum we want to get the key & ordinal from.
 * @param key this is what we want the index of, so we can return the ordinal.
 */
export function getOrdinal(enumeration: any, key: string ): number {
  return Object.keys(enumeration).indexOf(key);
}



