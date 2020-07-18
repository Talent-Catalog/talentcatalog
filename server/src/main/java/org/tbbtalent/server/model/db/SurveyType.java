/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "survey_type")
@SequenceGenerator(name = "seq_gen", sequenceName = "survey_type_id_seq", allocationSize = 1)
public class SurveyType  extends AbstractTranslatableDomainObject<Long> {

    @Enumerated(EnumType.STRING)
    private Status status;

    public SurveyType() {
    }

    public SurveyType(String name, Status status) {
        setName(name);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
