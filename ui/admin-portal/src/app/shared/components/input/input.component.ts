import {
  Component,
  ElementRef,
  EventEmitter,
  forwardRef,
  Input,
  OnInit,
  Output,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {Observable} from "rxjs";
import {NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

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
  @Input() placeholder: string = '';
  @Input() invalid: boolean = false;
  @Input() defaultValue: string = '';
  @Input() ngbTypeahead!: (text$: Observable<string>) => Observable<any[]>;
  @Input() resultTemplate?: TemplateRef<any>;
  @Input() inputFormatter?: (value: any) => string;
  @Input() resultFormatter?: (value: any) => string;
  @Input() editable: boolean;
  @Input() readonly: boolean = false;
  @Input() type:
    | 'text'
    | 'password'
    | 'search'
    | 'tel'
    | 'url'
    | 'email'
    | 'number'
    | 'range'
    | 'color'
    | 'date'
    | 'month'
    | 'week'
    | 'time'
    | 'datetime-local'
    | 'checkbox'
    | 'radio'
    | 'file'
    | 'button'
    | 'submit'
    | 'reset'
    | 'hidden'
    | 'image' = 'text';

  /** Disabled state coming from an input e.g. [disabled]="loading" */
  @Input() set disabled(val: boolean) {
    this._disabledInput = val;
    this.updateDisabledState();
  }

  get disabled(): boolean {
    return this._disabledFinal;
  }

  /** The input can be disabled in two ways:
   * - input: _disabledInput eg. <tc-input formControlName="email" disabled="true">
   * - form control: _disabledFromForm eg. this.form.get('email').disable();
   * Both disabled states need to be tracked and if both are applied we need one to take precedence,
   * so use disabledFinal for the final disabled state.
   * */
  private _disabledInput = false;
  private _disabledFromForm = false;
  private _disabledFinal = false;

  @Output() valueChange = new EventEmitter<string | boolean>();

  @Output() selectItem =
    new EventEmitter<NgbTypeaheadSelectItemEvent<any>>();

  @ViewChild('inputEl', { static: false }) inputEl!: ElementRef<HTMLInputElement>;

  protected _value: string | boolean = '';

  get value(): string | boolean {
    return this._value;
  }

  set value(val: string | boolean) {
    if (val !== this._value) {
      this._value = val;
      this.onChange(val);
      this.valueChange.emit(this.type === 'checkbox' ? val as boolean : val as string);
    }
  }

  private onChange = (val: any) => {};
  private onTouched = () => {};

  ngOnInit() {
    // Only set default value if no value has been set by the form control
    if (this.defaultValue && !this._value) {
      this.value = this.defaultValue;
    }
    this.updateDisabledState();
  }

  // ControlValueAccessor implementation
  writeValue(val: any): void {
    if (this.type === 'checkbox') {
      this._value = Boolean(val);
    } else {
      this._value = val ?? '';
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  /** This method is called when the angular form control .disable() method is used */
  setDisabledState(isDisabled: boolean): void {
    this._disabledFromForm = isDisabled;
    this.updateDisabledState();
  }

  /** Form control disabled state takes precedence */
  private updateDisabledState() {
    this._disabledFinal = this._disabledFromForm || this._disabledInput;
  }

  handleInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.value = this.type === 'checkbox' ? target.checked : target.value;
  }

  handleBlur() {
    this.onTouched();
  }

  focus(): void {
    if (this.inputEl?.nativeElement) {
      this.inputEl.nativeElement.focus();
    }
  }
}
