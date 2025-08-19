import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewMetadataTaskComponent } from './view-metadata-task.component';

describe('ViewMetadataTaskComponent', () => {
  let component: ViewMetadataTaskComponent;
  let fixture: ComponentFixture<ViewMetadataTaskComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ViewMetadataTaskComponent]
    });
    fixture = TestBed.createComponent(ViewMetadataTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
