import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PreferredPathwaysComponent} from './preferred-pathways.component';

describe('PreferredPathwaysComponent', () => {
  let component: PreferredPathwaysComponent;
  let fixture: ComponentFixture<PreferredPathwaysComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PreferredPathwaysComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PreferredPathwaysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
