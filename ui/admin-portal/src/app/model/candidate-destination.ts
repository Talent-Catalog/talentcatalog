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

import {Country} from './country';
import {Candidate, CandidateIntakeData, FamilyRelations, YesNoUnsure} from './candidate';

export interface CandidateDestination {
  id?: number;
  country?: Country;
  candidate?: Candidate;
  interest?: YesNoUnsure;
  family?: FamilyRelations;
  location?: string;
  notes?: string;
}

export function describeFamilyInDestination(countryId: number, candidateIntakeData: CandidateIntakeData): string {
  let dest = candidateIntakeData?.candidateDestinations.find(d => d.country.id == countryId)
  let family: string = 'No family entered'
  if (dest?.family) {
    if (dest?.location) {
      family = dest?.family + ' in ' + dest?.location;
    } else {
      family = dest?.family;
    }
    return family;
  }
  return family;
}
