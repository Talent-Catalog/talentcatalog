import {ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaPathwaysComponent} from './visa-pathways.component';

describe('VisaPathwaysComponent', () => {
  let component: VisaPathwaysComponent;
  let fixture: ComponentFixture<VisaPathwaysComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaPathwaysComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaPathwaysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
