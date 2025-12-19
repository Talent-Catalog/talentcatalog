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

package org.tctalent.server.request.job;

/**
 * Implements getter interface for job intake data fields.
 * <p/>
 * This interface is shared by JobIntakeData and the JobOppIntake entity allowing us to use them
 * interchangeably as a source of intake data.
 *
 * @author John Cameron
 */
public interface IJobIntakeData {
    Long getId();
    String getSalaryRange();
    String getRecruitmentProcess();
    String getEmployerCostCommitment();
    String getLocation();
    String getLocationDetails();
    String getBenefits();
    String getLanguageRequirements();
    String getEducationRequirements();
    String getSkillRequirements();
    String getEmploymentExperience();
    String getOccupationCode();
    String getMinSalary();
    String getVisaPathways();
}
