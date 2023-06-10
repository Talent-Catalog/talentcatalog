import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CreatedByComponent} from './created-by.component';

describe('CreatedByComponent', () => {
  let component: CreatedByComponent;
  let fixture: ComponentFixture<CreatedByComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreatedByComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreatedByComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
