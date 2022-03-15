import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewQuestionTaskComponent} from './view-question-task.component';

describe('ViewQuestionTaskComponent', () => {
  let component: ViewQuestionTaskComponent;
  let fixture: ComponentFixture<ViewQuestionTaskComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewQuestionTaskComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewQuestionTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
