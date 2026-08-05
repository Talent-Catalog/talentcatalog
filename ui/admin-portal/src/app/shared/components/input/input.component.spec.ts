import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InputComponent } from './input.component';

describe('InputComponent', () => {
  let component: InputComponent;
  let fixture: ComponentFixture<InputComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [InputComponent]
    });
    fixture = TestBed.createComponent(InputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should pass min, max and step through to the native input', () => {
    component.type = 'range';
    component.min = 0;
    component.max = 1;
    component.step = 0.1;
    fixture.detectChanges();

    const inputEl: HTMLInputElement = fixture.nativeElement.querySelector('input');
    expect(inputEl.min).toBe('0');
    expect(inputEl.max).toBe('1');
    expect(inputEl.step).toBe('0.1');
  });
});
