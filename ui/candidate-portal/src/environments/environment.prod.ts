/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

// This file replaces environment.ts during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  firebase: {
    projectId: 'fortesttccatalog',
    appId: '1:47912608172:web:a2e5df8650c429ada4da09',
    storageBucket: 'fortesttccatalog.appspot.com',
    apiKey: 'AIzaSyBEaYGOPYET2gcLWjiCTgnTCpQp3MYPr24',
    authDomain: 'fortesttccatalog.firebaseapp.com',
    messagingSenderId: '47912608172',
    vapidKey: 'BJJ8PlNzhC4ATAahI1-jNVOOrkpmhYJ4AWhXpsNTU4QA_l6sbTOamPPVgIiYPtDWvY8bHRYuxoKSqyTiB58DxyY'
  },
  production: true,
  host: document.location.host,
  publishUrl: document.location.origin + '/published',
  chatApiUrl: '/api/admin',
  apiUrl: '/api/portal',
  systemApiUrl: '/api/system',
  s3BucketUrl: 'https://s3.us-east-1.amazonaws.com/files.tbbtalent.org',
  googleAnalyticsId: 'G-BPDYWB77Y3'
};
