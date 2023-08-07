import {ComponentFixture, TestBed} from '@angular/core/testing';

import {IneligiblePathwaysComponent} from './ineligible-pathways.component';

describe('IneligiblePathwaysComponent', () => {
  let component: IneligiblePathwaysComponent;
  let fixture: ComponentFixture<IneligiblePathwaysComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IneligiblePathwaysComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IneligiblePathwaysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
