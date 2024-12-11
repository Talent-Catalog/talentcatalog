/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ViewChatComponent} from "./view-chat.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {JobChat, JobChatType} from "../../../model/chat";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {TranslateModule} from "@ngx-translate/core";

describe('ViewChatComponent', () => {
  let component: ViewChatComponent;
  let fixture: ComponentFixture<ViewChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, TranslateModule.forRoot({})],
      declarations: [ViewChatComponent],
      schemas: [NO_ERRORS_SCHEMA] // To ignore subcomponent and directive errors
    }).compileComponents();

    fixture = TestBed.createComponent(ViewChatComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display chat-read-status component with correct input', () => {
    const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect, name: 'Test Chat' };
    component.chat = mockChat;

    fixture.detectChanges();

    const chatReadStatusComponent = fixture.debugElement.nativeElement.querySelector('app-chat-read-status');
    expect(chatReadStatusComponent).toBeTruthy();
  });
});
