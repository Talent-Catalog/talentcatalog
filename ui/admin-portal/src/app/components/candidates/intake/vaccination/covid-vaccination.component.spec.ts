import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CovidVaccinationComponent} from './covid-vaccination.component';

describe('CovidVaccinationComponent', () => {
  let component: CovidVaccinationComponent;
  let fixture: ComponentFixture<CovidVaccinationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CovidVaccinationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CovidVaccinationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
