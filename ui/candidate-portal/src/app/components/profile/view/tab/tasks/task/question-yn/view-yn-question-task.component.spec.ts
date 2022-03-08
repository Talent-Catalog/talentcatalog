import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewYnQuestionTaskComponent} from './view-yn-question-task.component';

describe('ViewYnQuestionTaskComponent', () => {
  let component: ViewYnQuestionTaskComponent;
  let fixture: ComponentFixture<ViewYnQuestionTaskComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewYnQuestionTaskComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewYnQuestionTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
