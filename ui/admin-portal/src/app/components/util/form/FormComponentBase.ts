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

import {Directive} from "@angular/core";
import {AbstractControl, UntypedFormBuilder} from "@angular/forms";

/**
 * Base class for component containing a form.
 * <p/>
 * Provides following standard functionality
 * <ul>
 *   <li>Provides standard isInvalid method</li>
 *   <li>Provide a FormBuilder instance {@link fb} that subclasses can use to create the form</li>
 * </ul>
 * @author John Cameron
 */
@Directive()
export abstract class FormComponentBase {

  protected constructor(protected fb: UntypedFormBuilder) {
  }

  /**
   * Returns true if the value in the given control is invalid and has been changed.
   * @param control Form control - eg text box, drop down etc
   */
  isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched)
  }
}
