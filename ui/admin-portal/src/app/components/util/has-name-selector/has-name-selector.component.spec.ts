import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {HasNameSelectorComponent} from './has-name-selector.component';

describe('IdNameSelectorComponent', () => {
  let component: HasNameSelectorComponent;
  let fixture: ComponentFixture<HasNameSelectorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HasNameSelectorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HasNameSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
