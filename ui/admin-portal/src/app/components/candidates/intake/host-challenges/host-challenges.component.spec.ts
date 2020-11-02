import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {HostChallengesComponent} from './host-challenges.component';

describe('HostChallengesComponent', () => {
  let component: HostChallengesComponent;
  let fixture: ComponentFixture<HostChallengesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HostChallengesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HostChallengesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
