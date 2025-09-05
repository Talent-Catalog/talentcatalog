import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewFormTaskComponent} from './view-form-task.component';

describe('FormComponent', () => {
  let component: ViewFormTaskComponent;
  let fixture: ComponentFixture<ViewFormTaskComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ViewFormTaskComponent]
    });
    fixture = TestBed.createComponent(ViewFormTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
