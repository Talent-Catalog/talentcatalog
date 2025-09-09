// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.model.db;

import java.io.Serializable;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Converts the string key sent to API into a composite key needed to access CandidateFormInstance's.
 *
 * @author John Cameron
 */

@Component
public class CandidateFormInstanceKeyConverter implements BackendIdConverter {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return CandidateFormInstance.class.isAssignableFrom(type);
    }

    @Override
    public Serializable fromRequestId(String candidateNumber, Class<?> entityType) {
        //TODO JC Debug hard coding key
        return new CandidateFormInstanceKey(27671L, 4L); // country, number
    }

    @Override
    public String toRequestId(Serializable id, Class<?> entityType) {
        CandidateFormInstanceKey pid = (CandidateFormInstanceKey) id;
        //TODO JC Hard coded
        return "8363";
    }
}
