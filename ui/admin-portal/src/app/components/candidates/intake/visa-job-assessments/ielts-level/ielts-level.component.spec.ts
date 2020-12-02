import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {IeltsLevelComponent} from './ielts-level.component';

describe('IeltsLevelComponent', () => {
  let component: IeltsLevelComponent;
  let fixture: ComponentFixture<IeltsLevelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IeltsLevelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IeltsLevelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
