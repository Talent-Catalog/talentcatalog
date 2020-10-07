import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RuralComponent} from './rural.component';

describe('RuralComponent', () => {
  let component: RuralComponent;
  let fixture: ComponentFixture<RuralComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RuralComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RuralComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
