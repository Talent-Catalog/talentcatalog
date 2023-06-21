/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import {CandidateOpportunityStage} from "./candidate-opportunity";

/**
 * Given the key of an Opportunity Stage enum (job or candidate), return the string value
 * abbreviated for easy display by stripping out any numeric prefix or parenthesized suffix.
 * <p/>
 * Just returns the input enumStageNameKey if it does not match any enum key name.
 * @param enumStageNameKey Key name of CandidateOpportunityStag
 */
export function getOpportunityStageName(enumStageNameKey: string): string {
  let s = CandidateOpportunityStage[enumStageNameKey];
  if (!s) {
    s = enumStageNameKey;
  } else {
    //Strip off extra stuff

    //Strip off any prefix - eg '17. '
    let prefixIndex = s.indexOf('. ');
    if (prefixIndex >= 0) {
      s = s.substring(prefixIndex + 2);
    }

    //Strip off any suffix - eg ' (Canada only)'
    let suffixIndex = s.indexOf(' (');
    if (suffixIndex >= 0) {
      s = s.substring(0, suffixIndex);
    }
  }
  return s;
}
