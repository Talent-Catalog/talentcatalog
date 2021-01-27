import {HasName} from './base';
import {EnumOption} from '../util/enum';

export interface Country extends HasName {
  id: number;
  name: string;
  status: string;
  translatedName: string;
}

/**
 * Takes an array of strings and interprets them as keys of the given string
 * enumeration, returning an  array of values and display text suitable for
 * options in an html <select>.
 * @param keys Array of strings treated as enumeration keys
 * @param enumeration String enumeration object
 */
export function getListOfIds(keys: string[], enumeration): EnumOption[] {
  return keys?.map(key => ({ value: key, displayText: enumeration[key] }));
}
