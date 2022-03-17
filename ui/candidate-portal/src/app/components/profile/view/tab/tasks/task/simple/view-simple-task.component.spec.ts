import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewSimpleTaskComponent} from './view-simple-task.component';

describe('ViewSimpleTaskComponent', () => {
  let component: ViewSimpleTaskComponent;
  let fixture: ComponentFixture<ViewSimpleTaskComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewSimpleTaskComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewSimpleTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
