/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

/**
 * Saves blob to local browser file system - typically to download folder.
 * <p/>
 * Note that Chrome browser can be configured to always open files of a certain
 * type - as indicated by filename suffix - eg pdf.
 * See https://stackoverflow.com/questions/35138424/how-do-i-download-a-file-with-angular2-or-greater
 * @param blob Blob to save
 * @param filename Name to save it with
 */
export function saveBlob(blob: Blob, filename: string) {
  if (navigator.msSaveBlob) {
    // IE 10+
    navigator.msSaveBlob(blob, filename);
  } else {
    const link = document.createElement('a');
    // Browsers that support HTML5 download attribute
    if (link.download !== undefined) {
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', filename);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }
}
