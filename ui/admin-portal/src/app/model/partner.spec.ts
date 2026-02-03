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

import {Partner, sourceCountriesAsString} from "./partner";
import {Country} from "./country";

describe('sourceCountriesAsString function', () => {
  it('should return a comma-separated string of country names', () => {
    const partner: Partner = {
      id: 1,
      name: 'Sample Partner',
      abbreviation: 'SP',
      websiteUrl: 'https://samplepartner.com',
      sourceCountries: [
        { id: 1, name: 'Country A', translatedName: 'CA' } as Country,
        { id: 2, name: 'Country B', translatedName: 'CB' } as Country
      ],
      autoAssignable: true,
      defaultPartnerRef: true,
      jobCreator: true,
      logo: 'logo-url',
      notificationEmail: 'partner@example.com',
      registrationLandingPage: 'https://samplepartner.com/register',
      sflink: 'SF12345',
      sourcePartner: true,
      status: 'Active',
    } as Partner;

    const result = sourceCountriesAsString(partner);
    expect(result).toBe('Country A, Country B');
  });

  it('should return an empty string if sourceCountries is null', () => {
    const partner: Partner = {
      id: 1,
      name: 'Sample Partner',
      abbreviation: 'SP',
      websiteUrl: 'https://samplepartner.com',
      sourceCountries: null,
      autoAssignable: true,
      defaultPartnerRef: true,
      jobCreator: true,
      logo: 'logo-url',
      notificationEmail: 'partner@example.com',
      registrationLandingPage: 'https://samplepartner.com/register',
      sflink: 'SF12345',
      sourcePartner: true,
      status: 'Active',
    } as Partner;

    const result = sourceCountriesAsString(partner);
    expect(result).toBe('');
  });

  it('should return an empty string if sourceCountries is empty array', () => {
    const partner: Partner = {
      id: 1,
      name: 'Sample Partner',
      abbreviation: 'SP',
      websiteUrl: 'https://samplepartner.com',
      sourceCountries: [],
      autoAssignable: true,
      defaultPartnerRef: true,
      jobCreator: true,
      logo: 'logo-url',
      notificationEmail: 'partner@example.com',
      registrationLandingPage: 'https://samplepartner.com/register',
      sflink: 'SF12345',
      sourcePartner: true,
      status: 'Active',
    } as Partner;

    const result = sourceCountriesAsString(partner);
    expect(result).toBe('');
  });
});
