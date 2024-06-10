import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PasteTcLinkComponent} from './paste-tc-link.component';

describe('PasteTcLinkComponent', () => {
  let component: PasteTcLinkComponent;
  let fixture: ComponentFixture<PasteTcLinkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PasteTcLinkComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PasteTcLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
