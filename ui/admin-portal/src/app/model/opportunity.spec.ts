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

import {Opportunity} from './opportunity';

describe('Opportunity Interface', () => {
  it('should create a valid Opportunity object', () => {
    const opportunity: Opportunity = {
      id: 1,
      name: 'Sample Opportunity',
      closed: false,
      won: true,
      createdDate: new Date(),
      updatedDate: new Date(),
      sfId: 'SF12345',
      nextStep: 'Interview scheduling',
      nextStepDueDate: new Date()
    };

    // Assertions
    expect(opportunity.id).toEqual(1);
    expect(opportunity.name).toEqual('Sample Opportunity');
    expect(opportunity.closed).toEqual(false);
    expect(opportunity.won).toEqual(true);
    expect(opportunity.sfId).toEqual('SF12345');
    expect(opportunity.nextStep).toEqual('Interview scheduling');
    expect(opportunity.nextStepDueDate instanceof Date).toBe(true);
  });
});
