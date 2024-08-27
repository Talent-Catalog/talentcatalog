import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationDestinationsComponent} from './registration-destinations.component';

describe('RegistrationDestinationsComponent', () => {
  let component: RegistrationDestinationsComponent;
  let fixture: ComponentFixture<RegistrationDestinationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RegistrationDestinationsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationDestinationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
