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

import {SurveyType} from "./survey-type";

describe('SurveyType Interface', () => {
  it('should have an id property of type number', () => {
    const surveyType: SurveyType = {
      id: 1,
      name: 'Customer Satisfaction',
    };

    expect(typeof surveyType.id).toBe('number');
    expect(surveyType.id).toBe(1);
  });

  it('should have a name property of type string', () => {
    const surveyType: SurveyType = {
      id: 1,
      name: 'Customer Satisfaction',
    };

    expect(typeof surveyType.name).toBe('string');
    expect(surveyType.name).toBe('Customer Satisfaction');
  });

  it('should correctly assign the values to the properties', () => {
    const surveyType: SurveyType = {
      id: 2,
      name: 'Employee Engagement',
    };

    expect(surveyType.id).toBe(2);
    expect(surveyType.name).toBe('Employee Engagement');
  });

});
