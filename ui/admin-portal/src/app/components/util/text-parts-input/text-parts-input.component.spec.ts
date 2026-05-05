import {Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {TextPartsInputComponent} from './text-parts-input.component';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, TextPartsInputComponent],
  template: `
    <app-text-parts-input [formControl]="control"></app-text-parts-input>
  `
})
class HostComponent {
  control = new FormControl<string>('I work electrician 5 years');
}

describe('TextPartsInputComponent (standalone)', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HostComponent] // 👈 standalone host too
    }).compileComponents();

    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('displays legacy string as original text', () => {
    const inputs = fixture.debugElement.queryAll(By.css('input'));

    expect(inputs[0].nativeElement.value).toBe('I work electrician 5 years');
    expect(inputs[1].nativeElement.value).toBe('');
    expect(inputs[2].nativeElement.value).toBe('');
  });

  it('updates the form control with encoded text parts', () => {
    const inputs = fixture.debugElement.queryAll(By.css('input'));

    inputs[1].nativeElement.value = 'I worked as an electrician.';
    inputs[1].nativeElement.dispatchEvent(new Event('input'));

    inputs[2].nativeElement.value = 'electrician, wiring';
    inputs[2].nativeElement.dispatchEvent(new Event('input'));

    fixture.detectChanges();

    const value = JSON.parse(host.control.value ?? '');

    expect(value).toEqual({
      parts: {
        original: 'I work electrician 5 years',
        tidied: 'I worked as an electrician.',
        keywords: ['electrician', 'wiring']
      }
    });
  });

  it('respects disabled state', () => {
    host.control.disable();
    fixture.detectChanges();

    const inputs = fixture.debugElement.queryAll(By.css('input'));

    expect(inputs.every(input => input.nativeElement.disabled)).toBeTrue();
  });
});
