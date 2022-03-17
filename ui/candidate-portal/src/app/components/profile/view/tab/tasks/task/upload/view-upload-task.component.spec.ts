import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewUploadTaskComponent} from './view-upload-task.component';

describe('ViewUploadTaskComponent', () => {
  let component: ViewUploadTaskComponent;
  let fixture: ComponentFixture<ViewUploadTaskComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewUploadTaskComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewUploadTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
