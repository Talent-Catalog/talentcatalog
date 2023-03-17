import {ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaPathwaysEmployerComponent} from './visa-pathways-employer.component';

describe('VisaPathwaysEmployerComponent', () => {
  let component: VisaPathwaysEmployerComponent;
  let fixture: ComponentFixture<VisaPathwaysEmployerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaPathwaysEmployerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaPathwaysEmployerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
