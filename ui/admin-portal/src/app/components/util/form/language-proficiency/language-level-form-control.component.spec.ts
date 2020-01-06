import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LanguageLevelFormControlComponent} from './language-level-form-control.component';

describe('LanguageProficiencyControlComponent', () => {
  let component: LanguageLevelFormControlComponent;
  let fixture: ComponentFixture<LanguageLevelFormControlComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LanguageLevelFormControlComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LanguageLevelFormControlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
