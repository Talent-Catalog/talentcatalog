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

import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {Injectable} from "@angular/core";
import {S3UploadParams} from "../model/s3-upload-params";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class S3HelperService {

  apiUrl = environment.systemApiUrl + '/upload';
  s3BucketUrl = environment.s3BucketUrl;

  constructor(private http: HttpClient) {
  }

  // Create a policy for an upload folder
  getUploadPolicy(folder?: string): Observable<S3UploadParams> {
    return this.http.get<S3UploadParams>(`${this.apiUrl}/policy/${folder}`);
  }

  uploadFileToTempFolder(s3: S3UploadParams, file: File): Promise<any> {
    const url = this.s3BucketUrl;

    // modeled after: https://github.com/ntheg0d/angular2_aws_s3/blob/master/app/upload/upload.component.ts
    const formData: FormData = new FormData();
    formData.append('key', 'temp/' + s3.objectKey + '/' + file.name);
    formData.append('acl', 'private');
    formData.append('Content-Type', this.getContentType(file.name));
    formData.append('AWSAccessKeyId', s3.key);
    formData.append('policy', s3.policy);
    formData.append('signature', s3.signature);
    formData.append('file', file);

    /* Wrap the upload request in a promise to handle async behaviour */
    return new Promise(function (resolve, reject) {
      /* Configure the request */
      const xhr: XMLHttpRequest = new XMLHttpRequest();
      xhr.open('POST', url, true);

      /* Add event listeners to the upload */
      xhr.onreadystatechange = function () {
        let status;
        let data;
        if (xhr.readyState === 4) {
          status = xhr.status;
          if (status === 200 || status === 204) {
            data = xhr.responseText;
            resolve(data);
          } else {
            console.log(status);
            reject(status);
          }
        }
      };
      // xhr.upload.addEventListener('progress', function(evt) {
      //   if (evt.lengthComputable) {
      //     var percentComplete = Math.floor(evt.loaded / evt.total * 100);
      //     console.log(percentComplete + '% - ' + evt.loaded + '/' + evt.total);
      //   }
      // }, false);

      /* Begin the upload */
      xhr.send(formData);
    });
  }

  private getContentType(fileName: string): string {
    const fileNameTokens = fileName.split('.');
    if (fileNameTokens.length > 1) {
      const extension = fileNameTokens[fileNameTokens.length - 1];
      if (extension === 'zip') {
        return 'application/zip';
      } else if (extension === 'jpg') {
        return 'image/jpeg';
      } else if (extension === 'png') {
        return 'image/png';
      }
    }
    return 'application/octet-stream';
  }

}
