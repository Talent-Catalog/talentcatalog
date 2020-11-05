import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {MilitaryServiceComponent} from './military-service.component';

describe('MilitaryServiceComponent', () => {
  let component: MilitaryServiceComponent;
  let fixture: ComponentFixture<MilitaryServiceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MilitaryServiceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MilitaryServiceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
