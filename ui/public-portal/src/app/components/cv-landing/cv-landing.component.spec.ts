import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CvLandingComponent } from './cv-landing.component';

describe('CvDisplayComponent', () => {
  let component: CvLandingComponent;
  let fixture: ComponentFixture<CvLandingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CvLandingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CvLandingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
