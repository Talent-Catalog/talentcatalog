import { Component, Input, Output, EventEmitter, forwardRef, OnInit } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

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

  @Output() valueChange = new EventEmitter<string>();

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
