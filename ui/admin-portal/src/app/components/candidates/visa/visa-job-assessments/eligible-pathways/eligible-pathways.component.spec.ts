import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EligiblePathwaysComponent} from './eligible-pathways.component';

describe('EligiblePathwaysComponent', () => {
  let component: EligiblePathwaysComponent;
  let fixture: ComponentFixture<EligiblePathwaysComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EligiblePathwaysComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EligiblePathwaysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
