import {Component, EventEmitter, forwardRef, Input, Output} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

/**
 * @component TcRadioComponent
 * @selector tc-radio
 * @description
 * A reusable radio button component that implements Angular's `ControlValueAccessor`
 * so it works seamlessly with Reactive Forms and Template-driven Forms.
 *
 * **Features**
 * - Works with `formControlName` / `ngModel`.
 * - Supports string, number, or boolean values.
 * - Emits a `change` event when the user selects a radio.
 * - Fully compatible with multiple radio groups on the same page.
 * - Can contain additional content in the label as it follows with <ng-content> (e.g. can add buttons/icons)
 *
 * @example
 * ```html
 * <tc-radio
 *   id="option1"
 *   name="myGroup"
 *   label="Option 1"
 *   value="1"
 *   formControlName="myControl"
 *   (change)="onChange($event)">
 * </tc-radio>
 * ```
 */
@Component({
  selector: 'tc-radio',
  templateUrl: './tc-radio.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TcRadioComponent),
      multi: true
    }
  ],
  styleUrls: ['./tc-radio.component.scss']
})
export class TcRadioComponent implements
  ControlValueAccessor {
  @Input() id!: string;
  @Input() name!: string;
  @Input() value!: string | number | boolean;
  @Input() label!: string;

  /** Emits whenever the user changes the selection */
  @Output() change = new EventEmitter<string | number | boolean>();

  innerValue: any;

  private onChange = (_: any) => {};
  private onTouched = () => {};

  // Called when user selects this radio
  onChangeValue(val: any) {
    this.innerValue = val;
    this.onChange(val);
    this.onTouched();
    this.change.emit(val); // emit event to parent
  }

  // ControlValueAccessor methods
  writeValue(val: any): void { this.innerValue = val; }
  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
}
