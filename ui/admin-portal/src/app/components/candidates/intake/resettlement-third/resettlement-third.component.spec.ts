import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ResettlementThirdComponent} from './resettlement-third.component';

describe('ResettlementThirdComponent', () => {
  let component: ResettlementThirdComponent;
  let fixture: ComponentFixture<ResettlementThirdComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResettlementThirdComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResettlementThirdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
