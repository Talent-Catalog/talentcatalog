import {Component, forwardRef} from '@angular/core';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TextParts, TextPartsCodec} from "../../../util/text-parts/text-parts";
import {NgxWigModule} from "ngx-wig";

/**
 * This component is designed to be used as a form control.
 * <p>
 * It is used to take a single string value - for example, a candidate's description of one
 * of their job experiences.
 * <p>
 * That text is then broken up into {@link TextParts} and displayed to the form user so that they
 * can view or edit the original text, or add or update tiedied up text without modifying the
 * candidate's original text.
 * <p>
 * After the data entry, all parts of the text are returned as a single string.
 * <p>
 *   Example:
 *   Instead of:
 *  <pre>
 *     <input id="description" class="form-control" formControlName="description">
 *  </pre>
 *
 *  you can use...
 *   <pre>
 *     <app-text-parts-input formControlName="description"></app-text-parts-input>
 *   </pre>
 *
 */
@Component({
  selector: 'app-text-parts-input',
  standalone: true,
  imports: [FormsModule, NgxWigModule],
  templateUrl: './text-parts-input.component.html',

  //This makes the component a provider for NG_VALUE_ACCESSOR, allowing it to be used as a form
  //control.
  //See also that the component implements ControlValueAccessor.
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TextPartsInputComponent),
      multi: true
    }
  ]
})
export class TextPartsInputComponent implements ControlValueAccessor {

  parts: TextParts = {
    original: '',
    tidied: '',
    keywords: []
  };

  disabled = false;

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  // Angular → Component
  writeValue(value: string | null): void {
    this.parts = {
      original: '',
      tidied: '',
      keywords: [],
      ...TextPartsCodec.read(value)
    };
  }

  // Component → Angular
  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  // ---- UI handlers ----

  updateOriginal(value: string): void {
    this.parts.original = value;
    this.emit();
  }

  updateTidied(value: string): void {
    this.parts.tidied = value;
    this.emit();
  }

  updateKeywords(value: string): void {
    this.parts.keywords = value
    .split(',')
    .map(v => v.trim())
    .filter(v => v.length > 0);

    this.emit();
  }

  keywordsText(): string {
    return this.parts.keywords?.join(', ') ?? '';
  }

  onBlur(): void {
    this.onTouched();
  }

  private emit(): void {
    this.onChange(TextPartsCodec.write(this.parts));
  }
}
