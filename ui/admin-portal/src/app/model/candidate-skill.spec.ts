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

import {CandidateSkill} from "./candidate-skill";

describe('CandidateSkill Interface', () => {
  it('should create an instance with correct properties', () => {
    const skill: CandidateSkill = {
      skill: 'Programming',
      timePeriod: 5,
    };

    expect(skill).toBeTruthy();
    expect(skill.skill).toBe('Programming');
    expect(skill.timePeriod).toBe(5);
  });

  it('should allow modification of properties', () => {
    const skill: CandidateSkill = {
      skill: 'Data Analysis',
      timePeriod: 3,
    };

    skill.skill = 'Machine Learning';
    skill.timePeriod = 2;

    expect(skill.skill).toBe('Machine Learning');
    expect(skill.timePeriod).toBe(2);
  });

  it('should handle edge cases like empty string skill', () => {
    const skill: CandidateSkill = {
      skill: '',
      timePeriod: 1,
    };

    expect(skill.skill).toBe('');
    expect(skill.timePeriod).toBe(1);
  });
});
