import {Component, EventEmitter, forwardRef, Input, OnInit, Output} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

/**
 * @component TextareaComponent
 * @selector tc-textarea
 * @description
 * A reusable textarea form control component with Angular Forms integration.
 * Implements `ControlValueAccessor` so it works seamlessly with reactive and template-driven forms.
 * Typically used inside the design system’s `<tc-field>` or `<tc-fieldset>` along with
 * `<tc-label>`, `<tc-description>`, and `<tc-error-message>` to ensure accessibility and consistency.
 *
 * **Features**
 * - Works with both reactive (`formControlName`) and template-driven (`ngModel`) forms
 * - Emits `valueChange` events for two-way binding (`[(value)]`)
 * - Supports `disabled` and `invalid` states (can also inherit from `<tc-fieldset [disabled]>`)
 * - Handles accessibility with optional `ariaLabel`
 * - Supports a `defaultValue` if no initial value is provided
 *
 * **Inputs**
 * - `id?: string` → Optional unique identifier for the `<textarea>`
 * - `name?: string` → Optional name attribute (useful in forms)
 * - `placeholder: string` → Placeholder text when empty (default: `''`)
 * - `ariaLabel?: string` → Accessibility label for screen readers
 * - `disabled: boolean` → Disables the textarea (default: `false`)
 * - `invalid: boolean` → Marks the textarea invalid (default: `false`)
 * - `value: string` → Current value of the textarea (default: `''`)
 * - `defaultValue: string` → Initial fallback value if none provided
 *
 * **Outputs**
 * - `valueChange: EventEmitter<string>` → Emits the new value on input
 *
 * **Methods**
 * - `writeValue(val: any)` → Sets the value programmatically
 * - `registerOnChange(fn: any)` → Registers change handler (forms API)
 * - `registerOnTouched(fn: any)` → Registers touched handler (forms API)
 * - `setDisabledState(isDisabled: boolean)` → Programmatically enables/disables
 * - `handleInput(event: Event)` → Handles input changes and emits updates
 *
 * @example
 * ```html
 * <!-- Inside a field with label & description -->
 * <tc-field>
 *   <tc-label>Textarea</tc-label>
 *   <tc-description>This is description</tc-description>
 *   <tc-textarea [disabled]="true"></tc-textarea>
 * </tc-field>
 *
 * <!-- Reactive form integration -->
 * <form [formGroup]="form" (ngSubmit)="form.markAllAsTouched()">
 *   <tc-fieldset [disabled]="true">
 *     <tc-field>
 *       <tc-label>Textarea</tc-label>
 *       <tc-textarea formControlName="textareaField"></tc-textarea>
 *       <tc-error-message
 *         *ngIf="form.get('textareaField')?.hasError('required') && form.get('textareaField')?.touched">
 *         This field is required.
 *       </tc-error-message>
 *
 *       <tc-label>Input</tc-label>
 *       <tc-input formControlName="inputField"></tc-input>
 *       <tc-error-message
 *         *ngIf="form.get('inputField')?.hasError('required') && form.get('inputField')?.touched">
 *         This field is required.
 *       </tc-error-message>
 *     </tc-field>
 *   </tc-fieldset>
 *
 *   <button type="submit">Submit</button>
 * </form>
 * ```
 */

@Component({
  selector: 'tc-textarea',
  templateUrl: './textarea.component.html',
  styleUrls: ['./textarea.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TextareaComponent),
      multi: true,
    }
  ]
})
export class TextareaComponent implements ControlValueAccessor, OnInit {
  @Input() id?: string;
  @Input() name?: string;
  @Input() placeholder: string = '';
  @Input() ariaLabel?: string;
  @Input() disabled: boolean = false;
  @Input() invalid: boolean = false;
  @Input() value: string = '';
  @Input() defaultValue: string = '';
  @Input() rows: string = '3';

  @Output() valueChange = new EventEmitter<string>();

  onChange = (val: any) => {};
  onTouched = () => {};

  ngOnInit() {
    if (this.defaultValue && !this.value) {
      this.value = this.defaultValue;
    }
  }

  writeValue(val: any): void {
    this.value = val ?? '';
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  handleInput(event: Event) {
    const newValue = (event.target as HTMLTextAreaElement).value;
    this.value = newValue;
    this.onChange(newValue);
    this.valueChange.emit(newValue);
  }
}
