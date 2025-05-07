/*
 * Copyright (c) 2024 Talent Catalog.
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
import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.NextStepWithDueDate;

/**
 * Some utilities for managing opportunity next steps
 *
 * @author John Cameron
 */
public class NextStepHelper {

    /**
     * Format used for date timestamp in next step audit stamp
     */
    public static DateTimeFormatter nextStepDateFormatter = DateTimeFormatter.ofPattern("ddMMMuu");

    /**
     * The audit name stamp starts with this string
     */
    public static String nextStepAuditNameDelimiter = " --";
    /**
     * The audit date stamp ends with this string
     */
    public static String nextStepAuditDateDelimiter = "| ";

    /**
     * If the requested next step is different from the current next step, the next step
     * will be updated. In that case we add special text:
     * <ul>
     *     <li> the audit name to the end of the stripped nextStep.</li>
     *     <li> the audit date to the start of the stripped nextStep. </li>
     * </ul>
     * This indicates who has made this change to the next step, and when.
     * This is useful for auditing purposes.
     * <p/>
     * This method performs this logic, returning the processed next step which is what should be
     * used as the new next step.
     * @param name Name of person initiating the next step update
     * @param date Date of the update
     * @param currentNextStep The current next step
     * @param requestedNextStep The requested new next step
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
                processedNextStep = constructNextStepAuditStamp(name, date, stripped);
            }
        }
        return processedNextStep;
    }

    /**
     * Strips of any existing audit stamp
     * @param requestedNextStep Requested new next step
     * @return Same text just with any existing audit stamp stripped off.
     */
    private static String removeExistingStamp(String requestedNextStep) {
        String stripped = requestedNextStep;
        final int endIndex = requestedNextStep.lastIndexOf(nextStepAuditNameDelimiter);
        final int startIndex = requestedNextStep.indexOf(nextStepAuditDateDelimiter);
        if (endIndex >= 0) {
            stripped = stripped.substring(0, endIndex);
        }
        if (startIndex >= 0) {
            stripped = stripped.substring(startIndex + 2);
        }
        return stripped;
    }

    /**
     * Constructs the audited next step from the given name, date and stripped next step
     * @param name Name of user initiating update
     * @param date Date to be used on timestamp
     * @param strippedNextStep Next step text to have audit added to
     * @return The next step text with the audit stamp text added
     */
    public static String constructNextStepAuditStamp(String name, LocalDate date, String strippedNextStep) {
        String dateStamp = nextStepDateFormatter.format(date);
        return dateStamp + nextStepAuditDateDelimiter + strippedNextStep + nextStepAuditNameDelimiter + name;
    }

    /**
     * Checks if the user-entered value for Next Step (i.e. absent the audit stamp) is different
     * between the current and requested Next Step.
     * @param currentNextStep current value
     * @param requestedNextStep requested value
     * @return boolean - true if different
     */
    public static boolean isNextStepDifferent(String currentNextStep,
        @NonNull String requestedNextStep) {
        if (currentNextStep == null) {
            // requestedNextStep is never null, so they must be different in this case.
            return true;
        }
        String currentNextStepStripped = removeExistingStamp(currentNextStep);
        String requestedNextStepStripped = removeExistingStamp(requestedNextStep);
        return !currentNextStepStripped.equals(requestedNextStepStripped);
    }

    /**
     * Checks requested {@link NextStepWithDueDate} and returns true if:
     * <ul>
     *  <li><em>Processed</em> Next Step is non-null and different to current value.</li>
     *  <li>Next Step Due Date is non-null and different to current value.</li>
     * <ul>
     */
    public static boolean isNextStepInfoChanged(
        NextStepWithDueDate requested,
        NextStepWithDueDate current
    ) {
        return isNextStepDueDateChanged(requested, current)
            || isProcessedNextStepChanged(requested, current);
    }

    private static boolean isNextStepDueDateChanged(
        NextStepWithDueDate requested,
        NextStepWithDueDate current
    ) {
        return requested.dueDate() != null && !requested.dueDate().equals(current.dueDate());
    }

    private static boolean isProcessedNextStepChanged(
        NextStepWithDueDate requested,
        NextStepWithDueDate current
    ) {
        return requested.nextStep() != null && !requested.nextStep().equals(current.nextStep());
    }

}
