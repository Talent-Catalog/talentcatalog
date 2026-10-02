/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, Input} from '@angular/core';
import {CandidateJobExperience} from '../../../../../../model/candidate-job-experience';

/**
 * Presentation-only display of a single candidate job experience.
 * <p/>
 * Used both in the candidate's occupation/experience view and wherever else a single experience
 * needs to be shown (e.g. the match explanation's experience popover). This component never
 * creates, edits or deletes experiences - callers that need such controls can project them
 * alongside the experience by marking the projected element with an {@code experienceActions}
 * attribute (it is laid out as a column beside the main experience details).
 */
@Component({
  selector: 'app-candidate-job-experience',
  templateUrl: './candidate-job-experience.component.html',
  styleUrls: ['./candidate-job-experience.component.scss']
})
export class CandidateJobExperienceComponent {

  @Input({required: true}) experience!: CandidateJobExperience;
}
