import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegionalAreaComponent} from './regional-area.component';

describe('RegionalAreaComponent', () => {
  let component: RegionalAreaComponent;
  let fixture: ComponentFixture<RegionalAreaComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegionalAreaComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegionalAreaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
