import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ChatsWithPostsComponent} from './chats-with-posts.component';

describe('ChatsWithPostsComponent', () => {
  let component: ChatsWithPostsComponent;
  let fixture: ComponentFixture<ChatsWithPostsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ChatsWithPostsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatsWithPostsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
