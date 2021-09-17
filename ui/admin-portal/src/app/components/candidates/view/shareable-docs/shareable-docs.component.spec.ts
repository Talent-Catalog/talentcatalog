import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ShareableDocsComponent} from './shareable-docs.component';

describe('ShareableDocsComponent', () => {
  let component: ShareableDocsComponent;
  let fixture: ComponentFixture<ShareableDocsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ShareableDocsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShareableDocsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
