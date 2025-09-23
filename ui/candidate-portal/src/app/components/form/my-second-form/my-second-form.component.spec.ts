import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MySecondFormComponent } from './my-second-form.component';

describe('MySecondFormComponent', () => {
  let component: MySecondFormComponent;
  let fixture: ComponentFixture<MySecondFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MySecondFormComponent]
    });
    fixture = TestBed.createComponent(MySecondFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
