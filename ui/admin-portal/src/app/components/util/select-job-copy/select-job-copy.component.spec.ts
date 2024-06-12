import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SelectJobCopyComponent} from './select-job-copy.component';

describe('SelectJobCopyComponent', () => {
  let component: SelectJobCopyComponent;
  let fixture: ComponentFixture<SelectJobCopyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectJobCopyComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectJobCopyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
