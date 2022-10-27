import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditJobContactComponent} from './edit-job-contact.component';

describe('EditJobContactComponent', () => {
  let component: EditJobContactComponent;
  let fixture: ComponentFixture<EditJobContactComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditJobContactComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditJobContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
