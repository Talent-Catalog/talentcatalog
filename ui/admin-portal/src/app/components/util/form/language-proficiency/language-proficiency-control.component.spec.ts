import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LanguageProficiencyControlComponent} from './language-proficiency-control.component';

describe('LanguageProficiencyControlComponent', () => {
  let component: LanguageProficiencyControlComponent;
  let fixture: ComponentFixture<LanguageProficiencyControlComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LanguageProficiencyControlComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LanguageProficiencyControlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
