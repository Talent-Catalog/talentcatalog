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

import {Component, EventEmitter, Input, Output, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {of} from 'rxjs';
import {CandidateOppsComponent} from './candidate-opps.component';
import {AuthorizationService} from '../../../../../../services/authorization.service';
import {ChatService} from '../../../../../../services/chat.service';
import {JobChatType} from '../../../../../../model/chat';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Pipe({name: 'truncate'})
class TruncatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'app-error', template: ''})
class ErrorStubComponent {
  @Input() error: unknown;
}

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStubComponent {
  @Input() loading = false;
}

@Component({selector: 'app-candidate-opp', template: ''})
class CandidateOppStubComponent {
  @Input() selectedOpp: unknown;
  @Input() candidate: unknown;
  @Output() back = new EventEmitter<void>();
}

@Component({selector: 'app-tab-header', template: '<ng-content></ng-content>'})
class TabHeaderStubComponent {}

@Component({selector: 'app-chat-read-status', template: ''})
class ChatReadStatusStubComponent {
  @Input() chats: unknown;
}

@Component({selector: 'tc-table', template: '<ng-content></ng-content>'})
class TcTableStubComponent {
  @Input() striped = false;
}

describe('CandidateOppsComponent', () => {
  let component: CandidateOppsComponent;
  let fixture: ComponentFixture<CandidateOppsComponent>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;

  const makeCandidate = () => ({
    id: 7,
    candidateOpportunities: [],
  } as any);

  const makeOpp = (id = 1, name = 'Chef role') => ({
    id,
    jobOpp: {id: id + 10, name},
  } as any);

  beforeEach(async () => {
    authorizationServiceSpy = jasmine.createSpyObj('AuthorizationService', ['canViewChats']);
    chatServiceSpy = jasmine.createSpyObj('ChatService', ['getOrCreate', 'removeDuplicateChats']);
    authorizationServiceSpy.canViewChats.and.returnValue(false);
    chatServiceSpy.getOrCreate.and.returnValue(of({id: 1, type: JobChatType.CandidateProspect} as any));
    chatServiceSpy.removeDuplicateChats.and.callFake((chats) => chats);

    await TestBed.configureTestingModule({
      declarations: [
        CandidateOppsComponent,
        TranslatePipeStub,
        TruncatePipeStub,
        ErrorStubComponent,
        TcLoadingStubComponent,
        CandidateOppStubComponent,
        TabHeaderStubComponent,
        ChatReadStatusStubComponent,
        TcTableStubComponent,
      ],
      providers: [
        {provide: AuthorizationService, useValue: authorizationServiceSpy},
        {provide: ChatService, useValue: chatServiceSpy},
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOppsComponent);
    component = fixture.componentInstance;
    component.candidate = makeCandidate();
    component.filteredOpps = [makeOpp()];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render tc-loading and tc-table when showing the opp list', () => {
    const loading = fixture.debugElement.query(By.directive(TcLoadingStubComponent)).componentInstance as TcLoadingStubComponent;
    const table = fixture.debugElement.query(By.directive(TcTableStubComponent)).componentInstance as TcTableStubComponent;
    expect(loading.loading).toBeFalsy();
    expect(table).toBeTruthy();
    expect(table.striped).toBeTrue();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
    expect(rows[0].textContent).toContain('Chef role');
  });

  it('should render the selected opportunity child component after clicking a row', () => {
    const opp = component.filteredOpps[0];

    fixture.nativeElement.querySelector('tbody tr').click();
    fixture.detectChanges();

    const child = fixture.debugElement.query(By.directive(CandidateOppStubComponent));
    expect(component.selectedOpp).toBe(opp);
    expect(child).toBeTruthy();
    expect((child.componentInstance as CandidateOppStubComponent).selectedOpp).toBe(opp);
    expect((child.componentInstance as CandidateOppStubComponent).candidate).toBe(component.candidate);
  });

  it('should emit refresh when unSelectOpp is called', () => {
    spyOn(component.refresh, 'emit');
    component.selectedOpp = component.filteredOpps[0];

    component.unSelectOpp();

    expect(component.selectedOpp).toBeNull();
    expect(component.refresh.emit).toHaveBeenCalled();
  });
});
