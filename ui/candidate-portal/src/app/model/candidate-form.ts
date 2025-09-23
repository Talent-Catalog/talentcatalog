/*
 * Copyright (c) 2025 Talent Catalog.
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

/**
 * Defines what a Candidate Form component should look like.
 *
 * MySecondFormComponent is an example of a candidate form component. If you look at that code,
 * you will see how it works. In particular note the onSubmit method which calls a form-specific
 * method in the CandidateFormService which will be processed on the server by Spring Data Rest
 * which auto-generates a form-specific API. A form-specific entity, MySecondForm.java, is updated.
 * That entity defines the data the form should gather and how it is stored. Each field is
 * stored either in a Candidate-related table or in Candidate.candidateProperties.
 *
 * Every candidate form component should have a <code>@Input readOnly: boolean</code> which makes
 * the form read-only and disables submitting the form.
 *
 * It should also have an <code>@Output submitted = new EventEmitter<form data> </code> which
 * emits an event containing the form's data when it is submitted.
 *
 * So typically a component that is being used as a candidate form should look something
 * like this...
 * ```
 * export class MySecondFormComponent
 *                    implements ICandidateFormComponent<MySecondFormData>, ... {
 *   //When present and true, the form can't be modified or submitted
 *   @Input() readOnly = false;
 *
 *   //Output event supplying the submitted data
 *   @Output() submitted = new EventEmitter<MySecondFormData>();
 * ```
 * The form component also needs to be added to the componentMap of CandidateFormService.
 * See the beginning of CandidateFormService...
 * ```
 * export class CandidateFormService {
 *
 *   // You need to add an entry to this map for each form that can be
 *   // displayed in a FormTask.
 *   // The mapping is from the name of the form to an Angular component.
 *   private componentMap: Record<string, any> = {
 *     'MySecondForm': MySecondFormComponent
 *   }
 * ```
 *
 * A candidate form can be related to a FormTask. This relation is stored with the
 * task definition.
 *
 * Candidate Forms and Candidate Form Tasks are currently defined directly in the database using
 * Flyways.
 *
 * Here is the definition of a CandidateForm:
 * ```
 * insert into candidate_form (name, description) values (
 *   'MySecondForm',
 *   'Enter city where you live and the colour of your hair');
 * ```
 * Note that the form is just a name and a description. The data in a form is defined in the
 * entity named after the form: <code>MySecondForm.java</code> for the above form.
 *
 *  Here is the definition of CandidateFormTask:
 *  ```
 * insert into task (name, display_name, task_type, description, ..., candidate_form_id)
 * values ('mySecondFormTask','Fill out my first form',
 *         'FormTask', 'This form asks you for some pretty useless information.',
 *         ..., (select id from candidate_form where name = 'MySecondForm'));
 *  ```
 */
export interface ICandidateFormComponent<T = unknown> {
  readOnly: boolean;
  submitted: import("@angular/core").EventEmitter<T>;
}

