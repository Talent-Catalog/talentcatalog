import {Component, EventEmitter, forwardRef, Input, Output} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

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
