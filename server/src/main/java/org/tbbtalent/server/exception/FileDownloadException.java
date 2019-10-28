package org.tbbtalent.server.exception;

public class FileDownloadException extends ServiceException {

    public FileDownloadException() {
        super("file_download_failed", "The requested file could not be downloaded");
    }
    public FileDownloadException(String message,
                                 Throwable cause) {
        super("file_download_failed", message, cause);
    }

    public FileDownloadException(Throwable cause) {
        super("file_download_failed", "The requested file could not be downloaded", cause);
    }
}
