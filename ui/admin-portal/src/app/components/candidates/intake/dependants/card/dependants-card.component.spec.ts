import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DependantsCardComponent} from './dependants-card.component';

describe('DependantsCardComponent', () => {
  let component: DependantsCardComponent;
  let fixture: ComponentFixture<DependantsCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DependantsCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DependantsCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
