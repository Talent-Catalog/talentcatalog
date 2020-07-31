// This file replaces environment.ts during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: true,
  apiUrl: '/api/portal',
  systemApiUrl: '/api/system',
  s3BucketUrl: 'https://s3.us-east-1.amazonaws.com/files.tbbtalent.org'
};
