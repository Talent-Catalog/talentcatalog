import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditJobInfoComponent} from './edit-job-info.component';

describe('EditJobContactComponent', () => {
  let component: EditJobInfoComponent;
  let fixture: ComponentFixture<EditJobInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditJobInfoComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditJobInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
