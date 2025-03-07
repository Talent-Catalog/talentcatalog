import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DuolingoComponent } from './duolingo.component';

describe('DuolingoComponent', () => {
  let component: DuolingoComponent;
  let fixture: ComponentFixture<DuolingoComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DuolingoComponent]
    });
    fixture = TestBed.createComponent(DuolingoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
