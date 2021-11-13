import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EnglishThresholdComponent} from './english-threshold.component';

describe('EnglishThresholdComponent', () => {
  let component: EnglishThresholdComponent;
  let fixture: ComponentFixture<EnglishThresholdComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EnglishThresholdComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EnglishThresholdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
