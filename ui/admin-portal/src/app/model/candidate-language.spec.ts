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

import {CandidateLanguage} from './candidate-language';
import {Language} from './language';
import {LanguageLevel} from './language-level';
import {Candidate} from './candidate';
import {MockCandidate} from "../MockData/MockCandidate";

describe('CandidateLanguage Interface', () => {
  it('should create a valid CandidateLanguage object', () => {
    const language: Language = {
      id: 1,
      name: 'English',
      status: 'active'
    };

    const languageLevel: LanguageLevel = {
      id: 1,
      name: 'France',
      level: 1,
      status: 'active'
    };

    const candidate: Candidate = new MockCandidate();

    const candidateLanguage: CandidateLanguage = {
      id: 1,
      candidate: candidate,
      language: language,
      spokenLevel: languageLevel,
      writtenLevel: languageLevel,
      migrationLanguage: 'EN'
    };

    expect(candidateLanguage).toBeDefined();
    expect(candidateLanguage.id).toBe(1);
    expect(candidateLanguage.candidate).toBe(candidate);
    expect(candidateLanguage.language).toBe(language);
    expect(candidateLanguage.spokenLevel).toBe(languageLevel);
    expect(candidateLanguage.writtenLevel).toBe(languageLevel);
    expect(candidateLanguage.migrationLanguage).toBe('EN');
  });

  it('should have correct types for all properties', () => {
    const language: Language = {
      id: 1,
      name: 'English',
      status: 'active'
    };


    const languageLevel: LanguageLevel = {
      id: 1,
      name: 'France',
      level: 2,
      status: 'active'
    };

    const candidate: Candidate = new MockCandidate();

    const candidateLanguage: CandidateLanguage = {
      id: 2,
      candidate: candidate,
      language: language,
      spokenLevel: languageLevel,
      writtenLevel: languageLevel,
      migrationLanguage: 'FR'
    };

    expect(typeof candidateLanguage.id).toBe('number');
    expect(typeof candidateLanguage.candidate).toBe('object');
    expect(typeof candidateLanguage.language).toBe('object');
    expect(typeof candidateLanguage.spokenLevel).toBe('object');
    expect(typeof candidateLanguage.writtenLevel).toBe('object');
    expect(typeof candidateLanguage.migrationLanguage).toBe('string');
  });
});
