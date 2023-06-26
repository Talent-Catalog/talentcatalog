import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CostCommitEmployerComponent } from './cost-commit-employer.component';

describe('CostCommitmentEmployerComponent', () => {
  let component: CostCommitEmployerComponent;
  let fixture: ComponentFixture<CostCommitEmployerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CostCommitEmployerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CostCommitEmployerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
