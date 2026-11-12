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

import { environment } from '../../environments/environment';
import { isLocalOrStaging, isVerifyPlusUiEnabled } from './verify-plus-ui';

describe('verify-plus-ui', () => {
    let originalEnvironmentName: string;

    beforeEach(() => {
        originalEnvironmentName = environment.environmentName;
    });

    afterEach(() => {
        environment.environmentName = originalEnvironmentName;
    });

    it('should return true from isLocalOrStaging in local', () => {
        environment.environmentName = 'local';
        expect(isLocalOrStaging()).toBe(true);
    });

    it('should return true from isLocalOrStaging in staging', () => {
        environment.environmentName = 'staging';
        expect(isLocalOrStaging()).toBe(true);
    });

    it('should return false from isLocalOrStaging in prod', () => {
        environment.environmentName = 'prod';
        expect(isLocalOrStaging()).toBe(false);
    });

    it('should enable Verify+ UI for GRN local', () => {
        environment.environmentName = 'local';
        expect(isVerifyPlusUiEnabled(true)).toBe(true);
    });

    it('should enable Verify+ UI for GRN staging', () => {
        environment.environmentName = 'staging';
        expect(isVerifyPlusUiEnabled(true)).toBe(true);
    });

    it('should disable Verify+ UI for GRN prod', () => {
        environment.environmentName = 'prod';
        expect(isVerifyPlusUiEnabled(true)).toBe(false);
    });

    it('should disable Verify+ UI for TBB local', () => {
        environment.environmentName = 'local';
        expect(isVerifyPlusUiEnabled(false)).toBe(false);
    });

    it('should disable Verify+ UI for TBB staging', () => {
        environment.environmentName = 'staging';
        expect(isVerifyPlusUiEnabled(false)).toBe(false);
    });
});
