import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DefineSearchComponent } from './define-search.component';

describe('DefineSearchComponent', () => {
  let component: DefineSearchComponent;
  let fixture: ComponentFixture<DefineSearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DefineSearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DefineSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
