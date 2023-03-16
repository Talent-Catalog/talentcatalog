import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OccupationCodeComponent} from './occupation-code.component';

describe('OccupationCodeComponent', () => {
  let component: OccupationCodeComponent;
  let fixture: ComponentFixture<OccupationCodeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OccupationCodeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OccupationCodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
