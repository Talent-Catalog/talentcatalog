import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PublishedDocColumnSelectorComponent} from './published-doc-column-selector.component';

describe('PublishedDocColumnSelectorComponent', () => {
  let component: PublishedDocColumnSelectorComponent;
  let fixture: ComponentFixture<PublishedDocColumnSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PublishedDocColumnSelectorComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PublishedDocColumnSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
