import { Component, Input, Output, EventEmitter, forwardRef, OnInit } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

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
