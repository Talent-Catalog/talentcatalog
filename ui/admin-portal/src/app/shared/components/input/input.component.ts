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
  @Input() value: string = '';
  @Input() defaultValue: string = '';

  @Output() valueChange = new EventEmitter<string>();

  // The reference for creating custom inputs using ControlValueAccessor https://blog.bitsrc.io/how-ive-created-custom-inputs-in-angular-16-43f4c2d37d07
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
  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState?(isDisabled: boolean): void { this.disabled = isDisabled; }

  handleInput(event: Event) {
    const newValue = (event.target as HTMLInputElement).value;
    this.value = newValue;
    this.onChange(newValue);
    this.valueChange.emit(newValue);
  }
}
