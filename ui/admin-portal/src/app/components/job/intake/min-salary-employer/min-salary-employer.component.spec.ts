import {ComponentFixture, TestBed} from '@angular/core/testing';

import {MinSalaryEmployerComponent} from './min-salary-employer.component';

describe('MinSalaryEmployerComponent', () => {
  let component: MinSalaryEmployerComponent;
  let fixture: ComponentFixture<MinSalaryEmployerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MinSalaryEmployerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MinSalaryEmployerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
