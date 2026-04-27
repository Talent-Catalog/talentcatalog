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
import {Reaction} from './reaction';
import {DisplayUser} from './user';
describe('Reaction Interface Tests', () => {
  let testReaction: Reaction;

  beforeEach(() => {
    // Sample data for testing
    const users: DisplayUser[] = [
      { id: 1, displayName: 'user1' },
      { id: 2, displayName: 'user2'}
    ];

    testReaction = {
      id: 1,
      emoji: '👍',
      users: users
    };
  });

  it('should create a Reaction object', () => {
    // Verify if testReaction is correctly defined
    expect(testReaction).toBeDefined();
    expect(testReaction.id).toBe(1);
    expect(testReaction.emoji).toBe('👍');
    expect(testReaction.users.length).toBe(2);
  });

  it('should have users with correct attributes', () => {
    // Verify individual user attributes
    expect(testReaction.users[0].id).toBe(1);
    expect(testReaction.users[0].displayName).toBe('user1');
    expect(testReaction.users[1].id).toBe(2);
    expect(testReaction.users[1].displayName).toBe('user2');
  });

});
