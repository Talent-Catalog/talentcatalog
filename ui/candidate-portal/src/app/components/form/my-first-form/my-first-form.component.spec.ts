import {ComponentFixture, TestBed} from '@angular/core/testing';

import {MyFirstFormComponent} from './my-first-form.component';

describe('MyFirstFormComponent', () => {
  let component: MyFirstFormComponent;
  let fixture: ComponentFixture<MyFirstFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MyFirstFormComponent]
    });
    fixture = TestBed.createComponent(MyFirstFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
