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

// Based on this article: https://mokkapps.de/blog/how-to-build-an-angular-app-once-and-deploy-it-to-multiple-environments
// Is initialised at startup, when it infers the environment from the url and set's the env vars used elsewhere

import {Injectable} from "@angular/core";

export enum Environment {
  Local = 'local',
  Prod = 'prod',
  Staging = 'staging'
}

@Injectable({
  providedIn: 'root'
})

export class EnvService {
  private _env: Environment
  private _sfLightningUrl: string

  get env(): Environment {
    return this._env
  }

  get sfLightningUrl(): string {
    return this._sfLightningUrl
  }

  constructor() {}

  init(): Promise<void> {
    return new Promise((resolve) => {
      this.setEnvVariables()
      resolve()
    })
  }

  private setEnvVariables(): void {
    const hostname = window && window.location && window.location.hostname

    if ((/^.*localhost.*/.test(hostname))) {
      this._env = Environment.Local
      this._sfLightningUrl = 'https://talentbeyondboundaries--sfstaging.sandbox.lightning.force.com/'
    } else if ((/^tctalent-test.org/.test(hostname))) {
      this._env = Environment.Staging
      this._sfLightningUrl = 'https://talentbeyondboundaries--sfstaging.sandbox.lightning.force.com/'
    } else if ((/^tctalent.org/.test(hostname))) {
      this._env = Environment.Prod
      this._sfLightningUrl = 'https://talentbeyondboundaries.lightning.force.com/'
    }
  }
}
