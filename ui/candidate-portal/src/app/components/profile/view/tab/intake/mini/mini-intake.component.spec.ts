import {ComponentFixture, TestBed} from '@angular/core/testing';

import {MiniIntakeComponent} from './mini-intake.component';

describe('MiniIntakeComponent', () => {
  let component: MiniIntakeComponent;
  let fixture: ComponentFixture<MiniIntakeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MiniIntakeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MiniIntakeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
