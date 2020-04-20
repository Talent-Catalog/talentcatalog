package org.tbbtalent.server.request.candidate.stat;

import java.time.LocalDate;

public class CandidateStatDateRequest {

    private LocalDate dateFrom;
    private LocalDate dateTo;

    public LocalDate getDateFrom() { return dateFrom; }

    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }

    public LocalDate getDateTo() { return dateTo; }

    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
}
