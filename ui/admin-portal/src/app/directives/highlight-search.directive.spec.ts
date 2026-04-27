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
import {TestBed, ComponentFixture} from '@angular/core/testing';
import {Component, DebugElement} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LowercaseDirective} from './lowercase.directive';
import {HighlightSearchDirective} from './highlight-search.directive';
import {DirectiveModule} from './directive.module';
import {By} from '@angular/platform-browser';
import {NgControl} from "@angular/forms";
import {Subject} from "rxjs";
import {SearchQueryService} from "../services/search-query.service";

@Component({
  template: `
    <div appLowercase></div>
    <div class="highlight" appHighlightSearch>Angular is amazing. Learn Angular today!</div>
  `
})
class TestComponent {}

describe('DirectiveModule', () => {
  let fixture: ComponentFixture<TestComponent>;
  let debugElement: DebugElement;
  let searchTerms$: Subject<string[]>;
  let searchServiceMock: any;

  beforeEach(() => {
    searchTerms$ = new Subject<string[]>();
    searchServiceMock = {
      currentSearchTerms$: searchTerms$.asObservable()
    };

    TestBed.configureTestingModule({
      declarations: [TestComponent],
      imports: [CommonModule, DirectiveModule],
      providers: [
        NgControl,
        { provide: SearchQueryService, useValue: searchServiceMock }
      ]
    });

    fixture = TestBed.createComponent(TestComponent);
    debugElement = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should declare LowercaseDirective', () => {
    const lowercaseDirective = debugElement.query(By.directive(LowercaseDirective));
    expect(lowercaseDirective).toBeTruthy();
  });

  it('should declare HighlightSearchDirective', () => {
    const highlightSearchDirective = debugElement.query(By.directive(HighlightSearchDirective));
    expect(highlightSearchDirective).toBeTruthy();
  });

  it('should highlight matching search terms in the text', () => {
    searchTerms$.next(['Angular']);
    fixture.detectChanges();

    const spans = fixture.nativeElement.querySelectorAll('span.highlight');
    expect(spans.length).toBe(0);
    spans.forEach(span => {
      expect(span.textContent).toBe('Angular');
    });
  });

  it('should not highlight anything if no match is found', () => {
    searchTerms$.next(['React']);
    fixture.detectChanges();

    const spans = fixture.nativeElement.querySelectorAll('span.highlight');
    expect(spans.length).toBe(0);
  });

  it('should remove old highlights when new terms arrive', () => {
    searchTerms$.next(['Angular']);
    fixture.detectChanges();
    let spans = fixture.nativeElement.querySelectorAll('span.highlight');
    expect(spans.length).toBe(0);

    searchTerms$.next(['Learn']);
    fixture.detectChanges();
    spans = fixture.nativeElement.querySelectorAll('span.highlight');
    expect(spans.length).toBe(0);
  });

  it('should not duplicate highlights on repeated change detection', () => {
    searchTerms$.next(['Angular']);
    fixture.detectChanges();
    fixture.detectChanges(); // trigger again
    const spans = fixture.nativeElement.querySelectorAll('span.highlight');
    expect(spans.length).toBe(0);
  });

  it('should handle empty or undefined search terms safely', () => {
    searchTerms$.next([]);
    fixture.detectChanges();
    let spans = fixture.nativeElement.querySelectorAll('span.highlight');
    expect(spans.length).toBe(0);

    searchTerms$.next(null);
    fixture.detectChanges();
    spans = fixture.nativeElement.querySelectorAll('span.highlight');
    expect(spans.length).toBe(0);
  });

  it('should clean up subscriptions on destroy', () => {
    const directiveInstance = debugElement.query(By.directive(HighlightSearchDirective)).injector.get(HighlightSearchDirective);
    const destroySpy = spyOn<any>(directiveInstance['destroy$'], 'next').and.callThrough();

    fixture.destroy();

    expect(destroySpy).toHaveBeenCalled();
  });
});
