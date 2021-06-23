import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FormControlConditionalComponent} from './ielts-score-validation.component';

describe('FormControlConditionalComponent', () => {
  let component: FormControlConditionalComponent;
  let fixture: ComponentFixture<FormControlConditionalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FormControlConditionalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FormControlConditionalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
