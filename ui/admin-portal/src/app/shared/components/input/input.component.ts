import {
  Component,
  EventEmitter,
  forwardRef,
  Input,
  OnInit,
  Output,
  TemplateRef
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {Observable} from "rxjs";
import {NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";

/**
 * @component InputComponent
 * @selector tc-input
 * @description
 * A reusable, accessible input component that implements Angular’s `ControlValueAccessor`
 * to work seamlessly with both Reactive Forms and Template-driven Forms.
 *
 * **Features**
 * - Standard HTML `<input>` under the hood with sane defaults
 * - Works with `formControlName` / `ngModel`
 * - Emits `valueChange` when the value updates
 * - For checkbox styling pass [checkbox]="true" to the parent tc-field component
 *
 * @example
 * ### Reactive Forms
 * ```html
 * <form [formGroup]="form">
 *   <label for="email">Email</label>
 *   <tc-input
 *     id="email"
 *     name="email"
 *     type="email"
 *     placeholder="you@example.com"
 *     formControlName="email"
 *     [invalid]="form.get('email')?.invalid && form.get('email')?.touched">
 *   </tc-input>
 * </form>
 * ```
 *
 * ### Template-driven
 * ```html
 * <label for="username">Username</label>
 * <tc-input
 *   id="username"
 *   name="username"
 *   [(ngModel)]="model.username"
 *   placeholder="Enter username"
 *   (valueChange)="onUsernameChange($event)">
 * </tc-input>
 * ```
 */

@Component({
  selector: 'tc-input',
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true,
    }
  ]
})
export class InputComponent implements ControlValueAccessor, OnInit {
  @Input() id?: string;
  @Input() ariaLabel?: string;
  @Input() name?: string;
  @Input() type: string = 'text';
  @Input() placeholder: string = '';
  @Input() disabled = false;
  @Input() invalid: boolean = false;
  @Input() defaultValue: string = '';
  @Input() ngbTypeahead!: (text$: Observable<string>) => Observable<any[]>;
  @Input() resultTemplate?: TemplateRef<any>;
  @Input() inputFormatter?: (value: any) => string;
  @Input() editable: boolean;

  @Output() valueChange = new EventEmitter<string>();
  @Output() selectItem =
    new EventEmitter<NgbTypeaheadSelectItemEvent<any>>();

  private _value: string = '';

  get value(): string {
    return this._value;
  }

  set value(val: string) {
    if (val !== this._value) {
      this._value = val;
      this.onChange(val);
      this.valueChange.emit(val);
    }
  }

  private onChange = (val: any) => {};
  private onTouched = () => {};

  ngOnInit() {
    // Only set default value if no value has been set by the form control
    if (this.defaultValue && !this._value) {
      this.value = this.defaultValue;
    }
  }

  // ControlValueAccessor implementation
  writeValue(val: any): void {
    this._value = val ?? '';
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  handleInput(event: Event) {
    const newValue = (event.target as HTMLInputElement).value;
    this.value = newValue;
  }

  handleBlur() {
    this.onTouched();
  }
}
