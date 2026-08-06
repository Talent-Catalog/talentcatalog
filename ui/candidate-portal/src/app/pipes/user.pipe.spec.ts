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

import {UserPipe} from './user.pipe';
import {User} from "../model/user";

describe('UserPipe', () => {
  let pipe: UserPipe;

  beforeEach(() => {
    pipe = new UserPipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return the full name for the fullName argument', () => {
    const user = {
      firstName: 'Ehsan',
      lastName: 'Ehrari'
    } as User;

    expect(pipe.transform(user, 'fullName'))
    .toBe('Ehsan Ehrari');
  });

  it('should use the full name and log for an unrecognised argument', () => {
    const consoleSpy = spyOn(console, 'log');
    const user = {
      firstName: 'Ehsan',
      lastName: 'Ehrari'
    } as User;

    const result = pipe.transform(user, 'unknown');

    expect(result).toBe('Ehsan Ehrari');
    expect(consoleSpy).toHaveBeenCalledOnceWith(
      '[User Pipe] Unrecognised argument',
      'unknown'
    );
  });

  it('should use the default switch path when no argument is provided', () => {
    const consoleSpy = spyOn(console, 'log');
    const user = {
      firstName: 'Ehsan',
      lastName: 'Ehrari'
    } as User;

    expect(pipe.transform(user)).toBe('Ehsan Ehrari');
    expect(consoleSpy).toHaveBeenCalledOnceWith(
      '[User Pipe] Unrecognised argument',
      undefined
    );
  });

  it('should return only the first name when the last name is missing', () => {
    const user = {
      firstName: 'Ehsan',
      lastName: null
    } as User;

    expect(pipe.transform(user, 'fullName')).toBe('Ehsan');
  });

  it('should return only the last name when the first name is missing', () => {
    const user = {
      firstName: null,
      lastName: 'Ehrari'
    } as User;

    expect(pipe.transform(user, 'fullName')).toBe('Ehrari');
  });

  it('should return an empty string when both names are missing', () => {
    const user = {
      firstName: null,
      lastName: null
    } as User;

    expect(pipe.transform(user, 'fullName')).toBe('');
  });

  it('should trim surrounding whitespace from the full name', () => {
    const user = {
      firstName: ' Ehsan',
      lastName: 'Ehrari '
    } as User;

    expect(pipe.transform(user, 'fullName'))
    .toBe('Ehsan Ehrari');
  });

  it('should return null when the user is null', () => {
    expect(pipe.transform(null as any, 'fullName')).toBeNull();
  });

  it('should return null when the user is undefined', () => {
    expect(pipe.transform(undefined as any, 'fullName')).toBeNull();
  });
});
