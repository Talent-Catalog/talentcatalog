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

package org.tctalent.server.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.annotation.Nullable;

/**
 * Some utilities for managing opportunity next steps
 *
 * @author John Cameron
 */
public class NextStepHelper {

    /**
     * Format used for date timestamp in next step audit stamp
     */
    public static DateTimeFormatter nextStepDateFormatter = DateTimeFormatter.ofPattern("yyMMdd");

    /**
     * The audit stamp starts with this string
     */
    public static String nextStepAuditStampDelimiter = " --";

    /**
     * If the requested next step is different from the current next step, the next step
     * will be updated. In that case we add special text (the audit timestamp) to the
     * end of the nextStep - indicating who has made this change to the next step, wand when.
     * This is useful for auditing purposes.
     * <p/>
     * This method performs this logic, returning the processed next step which is what should be
     * used as the new next step.
     * @param name Name of person initiating the next step update
     * @param date Date of the update
     * @param currentNextStep The current next step
     * @param requestedNextStep Thw requested new next step
     * @return The processed text which should be used for the next step update
     */
    public static String auditStampNextStep(String name, LocalDate date,
        @Nullable String currentNextStep, @Nullable String requestedNextStep) {
        //Initialize the processedNextStep tp the current next step
        String processedNextStep = currentNextStep;

        //If the requestedNextStep is null, we do no processing - returning the current next step
        //- ie no change.
        if (requestedNextStep != null) {
            //If the next steps are the same - do nothing
            if (!requestedNextStep.equals(currentNextStep)) {
                //We are doing an update.
                //Strip off any existing stamp. It is possible that there is a audit stamp
                //on the new next step. This can happen if the user does the update by editing
                //the existing next step. We want to get rid of the old audit stamp.
                String stripped = removeExistingStamp(requestedNextStep);

                //Now just add the new audit stamp on to the stripped version of the requested next
                //step,
                processedNextStep = stripped + constructNextStepAuditStamp(name, date);
            }
        }
        return processedNextStep;
    }

    /**
     * Strips of any existing audit time stamp
     * @param requestedNextStep Requested new next step
     * @return Same text just with any existing next step stripped off.
     */
    private static String removeExistingStamp(String requestedNextStep) {
        String stripped = requestedNextStep;
        final int endIndex = requestedNextStep.lastIndexOf(nextStepAuditStampDelimiter);
        if (endIndex >= 0) {
            stripped = requestedNextStep.substring(0, endIndex);
        }
        return stripped;
    }

    /**
     * Constructs the audit stamp from the given name and date
     * @param name Name of user initiating update
     * @param date Date to be used on timestamp
     * @return The audit stamp text
     */
    public static String constructNextStepAuditStamp(String name, LocalDate date) {
        String dateStamp = nextStepDateFormatter.format(date);
        return nextStepAuditStampDelimiter + dateStamp + " " + name;
    }

}
