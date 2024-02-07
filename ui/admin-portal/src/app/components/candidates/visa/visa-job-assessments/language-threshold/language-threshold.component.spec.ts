import {ComponentFixture, TestBed} from '@angular/core/testing';

import {LanguageThresholdComponent} from './language-threshold.component';

describe('LanguageThresholdComponent', () => {
  let component: LanguageThresholdComponent;
  let fixture: ComponentFixture<LanguageThresholdComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LanguageThresholdComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LanguageThresholdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
