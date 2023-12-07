import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RelocatingDependantsComponent} from './relocating-dependants.component';

describe('RelocatingDependantsComponent', () => {
  let component: RelocatingDependantsComponent;
  let fixture: ComponentFixture<RelocatingDependantsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RelocatingDependantsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RelocatingDependantsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
