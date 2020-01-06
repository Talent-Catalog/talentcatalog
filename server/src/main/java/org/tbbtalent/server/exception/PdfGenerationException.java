package org.tbbtalent.server.exception;

public class PdfGenerationException extends ServiceException {

    public PdfGenerationException(String message) {
        super("pdf_generation", message);

    }
}
