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

import {Environment, EnvService} from './env.service';

describe('EnvService', () => {
  let service: EnvService;

  const stagingSfUrl =
    'https://talentbeyondboundaries--sfstaging.sandbox.lightning.force.com/';

  const productionSfUrl =
    'https://talentbeyondboundaries.lightning.force.com/';

  const stagingDashboardId =
    '86f9d8cb-44a9-48fc-b516-eab1f87fc097';

  const productionDashboardId =
    '3d577f48-a4db-4e7d-95ed-6590d76829cc';

  beforeEach(() => {
    service = new EnvService();
  });

  it('should create the service', () => {
    expect(service).toBeTruthy();
  });

  it('should have undefined values before initialization', () => {
    expect(service.env).toBeUndefined();
    expect(service.sfLightningUrl).toBeUndefined();
    expect(service.allCandidatesDashboardId).toBeUndefined();
    expect(service.presetWorkspaceId).toBeUndefined();
  });

  it('should initialize the local environment', async () => {
    const regexTestSpy = spyOn(
      RegExp.prototype,
      'test'
    ).and.returnValues(true);

    await service.init();

    expect(regexTestSpy).toHaveBeenCalledTimes(1);
    expect(service.env).toBe(Environment.Local);
    expect(service.sfLightningUrl).toBe(stagingSfUrl);
    expect(service.allCandidatesDashboardId)
    .toBe(stagingDashboardId);
    expect(service.presetWorkspaceId).toBe('987e2e02');
  });

  it('should initialize staging for tctalent-test.org', async () => {
    const regexTestSpy = spyOn(
      RegExp.prototype,
      'test'
    ).and.returnValues(
      false,
      true
    );

    await service.init();

    expect(regexTestSpy).toHaveBeenCalledTimes(2);
    expect(service.env).toBe(Environment.Staging);
    expect(service.sfLightningUrl).toBe(stagingSfUrl);
    expect(service.allCandidatesDashboardId)
    .toBe(stagingDashboardId);
    expect(service.presetWorkspaceId).toBe('987e2e02');
  });

  it('should initialize staging for test.plus.tctalent.org', async () => {
    const regexTestSpy = spyOn(
      RegExp.prototype,
      'test'
    ).and.returnValues(
      false,
      false,
      true
    );

    await service.init();

    expect(regexTestSpy).toHaveBeenCalledTimes(3);
    expect(service.env).toBe(Environment.Staging);
    expect(service.sfLightningUrl).toBe(stagingSfUrl);
    expect(service.allCandidatesDashboardId)
    .toBe(stagingDashboardId);
    expect(service.presetWorkspaceId).toBe('987e2e02');
  });

  it('should initialize production for tctalent.org', async () => {
    const regexTestSpy = spyOn(
      RegExp.prototype,
      'test'
    ).and.returnValues(
      false,
      false,
      false,
      true
    );

    await service.init();

    expect(regexTestSpy).toHaveBeenCalledTimes(4);
    expect(service.env).toBe(Environment.Prod);
    expect(service.sfLightningUrl).toBe(productionSfUrl);
    expect(service.allCandidatesDashboardId)
    .toBe(productionDashboardId);
    expect(service.presetWorkspaceId).toBe('effaaec0');
  });

  it('should initialize production for plus.tctalent.org', async () => {
    const regexTestSpy = spyOn(
      RegExp.prototype,
      'test'
    ).and.returnValues(
      false,
      false,
      false,
      false,
      true
    );

    await service.init();

    expect(regexTestSpy).toHaveBeenCalledTimes(5);
    expect(service.env).toBe(Environment.Prod);
    expect(service.sfLightningUrl).toBe(productionSfUrl);
    expect(service.allCandidatesDashboardId)
    .toBe(productionDashboardId);
    expect(service.presetWorkspaceId).toBe('effaaec0');
  });

  it('should leave environment values undefined for an unknown hostname', async () => {
    const regexTestSpy = spyOn(
      RegExp.prototype,
      'test'
    ).and.returnValues(
      false,
      false,
      false,
      false,
      false
    );

    await service.init();

    expect(regexTestSpy).toHaveBeenCalledTimes(5);
    expect(service.env).toBeUndefined();
    expect(service.sfLightningUrl).toBeUndefined();
    expect(service.allCandidatesDashboardId).toBeUndefined();
    expect(service.presetWorkspaceId).toBeUndefined();
  });

  it('should resolve init with undefined', async () => {
    spyOn(RegExp.prototype, 'test').and.returnValue(true);

    const result = await service.init();

    expect(result).toBeUndefined();
  });
});
