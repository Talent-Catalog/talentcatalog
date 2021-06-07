import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SalesforceStageComponent } from './salesforce-stage.component';

describe('SalesforceStageComponent', () => {
  let component: SalesforceStageComponent;
  let fixture: ComponentFixture<SalesforceStageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SalesforceStageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SalesforceStageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
