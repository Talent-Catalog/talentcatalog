import {ComponentFixture, TestBed} from '@angular/core/testing';

import {MinSalaryComponent} from './min-salary.component';

describe('MinSalaryComponent', () => {
  let component: MinSalaryComponent;
  let fixture: ComponentFixture<MinSalaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MinSalaryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MinSalaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
