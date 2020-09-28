
/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

/**
 * Takes a string enumeration and returns a corresponding array of values and
 * display text suitable for options in an html <select>.
 * @param enumeration String enumeration object
 */
export function enumOptions(enumeration) {
  return Object.keys(enumeration)
    .map(key => ({ value: key, displayText: enumeration[key] }));
}
