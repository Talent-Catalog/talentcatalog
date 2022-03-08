import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewSimpleDocTaskComponent} from './view-simple-doc-task.component';

describe('ViewSimpleDocTaskComponent', () => {
  let component: ViewSimpleDocTaskComponent;
  let fixture: ComponentFixture<ViewSimpleDocTaskComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewSimpleDocTaskComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewSimpleDocTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
