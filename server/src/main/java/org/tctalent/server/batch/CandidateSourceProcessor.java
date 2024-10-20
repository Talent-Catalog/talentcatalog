/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.batch;

import org.springframework.stereotype.Component;
import org.tctalent.server.util.batch.BatchContext;
import org.tctalent.server.util.batch.BatchProcessor;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Component
public class CandidateSourceProcessor implements BatchProcessor {

    @Override
    public boolean process(BatchContext context) {
        boolean completed;
        if (context instanceof CandidateSourceProcessorContext) {
            completed = process((CandidateSourceProcessorContext) context);
        } else {
            //todo log error
            completed = true;
        }
        return completed;
    }

    public boolean process(CandidateSourceProcessorContext context) {
       //todo
       return true;
    }
}
