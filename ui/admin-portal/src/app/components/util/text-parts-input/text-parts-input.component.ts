import {Component, DestroyRef, forwardRef, inject, Input} from '@angular/core';
import {
  ControlValueAccessor,
  FormControl,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule
} from '@angular/forms';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NgxWigModule} from 'ngx-wig';
import {TextParts, TextPartsCodec} from "../../../util/text-parts/text-parts";
import {NgIf} from "@angular/common";

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
 *  or...
 *
 *  <pre>
 *      <ngx-wig id="description" formControlName="description"></ngx-wig>
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
  imports: [
    ReactiveFormsModule,
    NgxWigModule,
    NgIf
  ],
  templateUrl: './text-parts-input.component.html',
  styleUrl: './text-parts-input.component.scss',

  //This makes the component a provider for NG_VALUE_ACCESSOR, allowing it to be used as a form
  //control.
  //See also that the component implements ControlValueAccessor.
  providers: [{
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TextPartsInputComponent),
      multi: true
  }]
})
export class TextPartsInputComponent implements ControlValueAccessor {
  @Input() hideKeywords: boolean = false;
  @Input() hideTidied: boolean = false;

  private readonly destroyRef = inject(DestroyRef);

  readonly originalControl = new FormControl<string>('', { nonNullable: true });
  readonly tidiedControl = new FormControl<string>('', { nonNullable: true });
  readonly keywordsControl = new FormControl<string>('', { nonNullable: true });

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};
  private writingValue = false;

  constructor() {
    this.originalControl.valueChanges
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe(() => this.emitChange());

    this.tidiedControl.valueChanges
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe(() => this.emitChange());

    this.keywordsControl.valueChanges
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe(() => this.emitChange());
  }

  writeValue(value: string | null): void {
    const parts = TextPartsCodec.read(value);

    this.writingValue = true;

    this.originalControl.setValue(parts.original ?? '', { emitEvent: false });
    this.tidiedControl.setValue(parts.tidied ?? '', { emitEvent: false });
    this.keywordsControl.setValue(parts.keywords?.join(', ') ?? '', { emitEvent: false });

    this.writingValue = false;
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    if (isDisabled) {
      this.originalControl.disable({ emitEvent: false });
      this.tidiedControl.disable({ emitEvent: false });
      this.keywordsControl.disable({ emitEvent: false });
    } else {
      this.originalControl.enable({ emitEvent: false });
      this.tidiedControl.enable({ emitEvent: false });
      this.keywordsControl.enable({ emitEvent: false });
    }
  }

  markTouched(): void {
    this.onTouched();
  }

  private emitChange(): void {
    if (this.writingValue) {
      return;
    }

    const parts: TextParts = {
      original: this.originalControl.value,
      tidied: this.tidiedControl.value,
      keywords: this.keywordsControl.value
      .split(',')
      .map(keyword => keyword.trim())
      .filter(keyword => keyword.length > 0)
    };

    this.onChange(TextPartsCodec.write(parts));
  }
}
