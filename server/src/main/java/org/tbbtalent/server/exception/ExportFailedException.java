package org.tbbtalent.server.exception;

public class ExportFailedException extends ServiceException {

    public ExportFailedException() {
        super("export_failed", "The export could not be downloaded");
    }

    public ExportFailedException(Throwable cause) {
        super("export_failed", "The export could not be downloaded", cause);
    }
}
