import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditOppComponent} from './edit-opp.component';

describe('EditOppComponent', () => {
  let component: EditOppComponent;
  let fixture: ComponentFixture<EditOppComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditOppComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditOppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
