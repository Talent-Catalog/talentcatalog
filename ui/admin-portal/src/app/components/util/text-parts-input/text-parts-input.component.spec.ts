import {Component, forwardRef} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {
  ControlValueAccessor,
  FormControl,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule
} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {TextPartsInputComponent} from './text-parts-input.component';
import {NgxWigModule} from "ngx-wig";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'ngx-wig',
  standalone: true,
  imports: [ReactiveFormsModule,CommonModule],
  template: `
    <textarea
      [formControl]="innerControl"
      (blur)="onTouched()">
    </textarea>
  `,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => MockNgxWigComponent),
    multi: true
  }]
})
class MockNgxWigComponent implements ControlValueAccessor {
  innerControl = new FormControl<string>('', { nonNullable: true });

  private onChange: (value: string) => void = () => {};
  onTouched: () => void = () => {};

  constructor() {
    this.innerControl.valueChanges.subscribe(value => this.onChange(value));
  }

  writeValue(value: string | null): void {
    this.innerControl.setValue(value ?? '', { emitEvent: false });
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    if (isDisabled) {
      this.innerControl.disable({ emitEvent: false });
    } else {
      this.innerControl.enable({ emitEvent: false });
    }
  }
}

@Component({
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    TextPartsInputComponent
  ],
  template: `
    <app-text-parts-input [formControl]="control"></app-text-parts-input>
  `
})
class HostComponent {
  control = new FormControl<string>('Candidate entered <b>HTML</b>');
}

describe('TextPartsInputComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        HostComponent
      ]
    })
    .overrideComponent(TextPartsInputComponent, {
      remove: {
        imports: [NgxWigModule]
      },
      add: {
        imports: [
          ReactiveFormsModule,
          CommonModule,
          MockNgxWigComponent
        ]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('displays legacy string as original HTML text', () => {
    const textareas = fixture.debugElement.queryAll(By.css('textarea'));
    const keywordInput = fixture.debugElement.query(By.css('input'));

    expect(textareas.length).toBe(2);
    expect(textareas[0].nativeElement.value).toBe('Candidate entered <b>HTML</b>');
    expect(textareas[1].nativeElement.value).toBe('');
    expect(keywordInput.nativeElement.value).toBe('');
  });

  it('updates the form control with encoded HTML text parts', () => {
    const textareas = fixture.debugElement.queryAll(By.css('textarea'));
    const keywordInput = fixture.debugElement.query(By.css('input'));

    textareas[1].nativeElement.value = '<p>Candidate worked as an electrician.</p>';
    textareas[1].nativeElement.dispatchEvent(new Event('input'));

    keywordInput.nativeElement.value = 'electrician, wiring';
    keywordInput.nativeElement.dispatchEvent(new Event('input'));

    fixture.detectChanges();

    const value = JSON.parse(host.control.value ?? '');

    expect(value).toEqual({
      parts: {
        original: 'Candidate entered <b>HTML</b>',
        tidied: '<p>Candidate worked as an electrician.</p>',
        keywords: ['electrician', 'wiring']
      }
    });
  });

  it('reads existing JSON text parts into the editor fields', () => {
    host.control.setValue(JSON.stringify({
      parts: {
        original: '<p>Original candidate text</p>',
        tidied: '<p>Tidied candidate text</p>',
        keywords: ['hospitality', 'cleaning']
      }
    }));

    fixture.detectChanges();

    const textareas = fixture.debugElement.queryAll(By.css('textarea'));
    const keywordInput = fixture.debugElement.query(By.css('input'));

    expect(textareas[0].nativeElement.value).toBe('<p>Original candidate text</p>');
    expect(textareas[1].nativeElement.value).toBe('<p>Tidied candidate text</p>');
    expect(keywordInput.nativeElement.value).toBe('hospitality, cleaning');
  });

  it('respects disabled state', () => {
    host.control.disable();
    fixture.detectChanges();

    const textareas = fixture.debugElement.queryAll(By.css('textarea'));
    const keywordInput = fixture.debugElement.query(By.css('input'));

    expect(textareas.every(textarea => textarea.nativeElement.disabled)).toBeTrue();
    expect(keywordInput.nativeElement.disabled).toBeTrue();
  });
});
