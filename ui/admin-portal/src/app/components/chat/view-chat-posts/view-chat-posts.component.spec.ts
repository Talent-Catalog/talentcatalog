import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewChatPostsComponent} from './view-chat-posts.component';

describe('ViewChatPostsComponent', () => {
  let component: ViewChatPostsComponent;
  let fixture: ComponentFixture<ViewChatPostsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewChatPostsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewChatPostsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
