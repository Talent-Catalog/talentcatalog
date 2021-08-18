import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OldIntakeInputComponent} from './old-intake-input.component';

describe('OldIntakeInputComponent', () => {
  let component: OldIntakeInputComponent;
  let fixture: ComponentFixture<OldIntakeInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OldIntakeInputComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OldIntakeInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
