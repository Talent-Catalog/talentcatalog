export interface S3UploadParams {
  objectKey: string; // Folder location where the file will be uploaded
  key: string;
  policy: string;
  signature: string;
}
