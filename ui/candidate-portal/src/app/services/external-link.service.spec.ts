/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ExternalLinkService} from './external-link.service';

describe('ExternalLinkService', () => {
  let service: ExternalLinkService;

  beforeEach(() => {
    service = new ExternalLinkService();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return the English eligibility link', () => {
    expect(service.getLink('eligibility', 'en')).toBe(
      'https://www.talentbeyondboundaries.org/talentcatalog/tbb-eligibility?lang=en'
    );
  });

  it('should return the Arabic eligibility link', () => {
    expect(service.getLink('eligibility', 'ar')).toBe(
      'https://www.talentbeyondboundaries.org/talentcatalog/tbb-eligibility?lang=ar'
    );
  });

  it('should return the Spanish eligibility link', () => {
    expect(service.getLink('eligibility', 'es')).toBe(
      'https://www.talentbeyondboundaries.org/talentcatalog/tbb-eligibility?lang=es'
    );
  });

  it('should fall back to English when the language is unavailable', () => {
    expect(service.getLink('eligibility', 'fa')).toBe(
      'https://www.talentbeyondboundaries.org/talentcatalog/tbb-eligibility?lang=en'
    );
  });

  it('should warn and return an empty string for an unknown category', () => {
    const warningSpy = spyOn(console, 'warn');

    const result = service.getLink('unknown', 'en');

    expect(result).toBe('');
    expect(warningSpy).toHaveBeenCalledOnceWith(
      'Category unknown not found.'
    );
  });

  it('should return an empty string when neither requested nor English links exist', () => {
    (service as any).links.emptyCategory = {};

    expect(service.getLink('emptyCategory', 'fa')).toBe('');
  });

  it('should use the requested language before the English fallback', () => {
    (service as any).links.testCategory = {
      en: 'english-link',
      fa: 'dari-link'
    };

    expect(service.getLink('testCategory', 'fa'))
    .toBe('dari-link');
  });
});
