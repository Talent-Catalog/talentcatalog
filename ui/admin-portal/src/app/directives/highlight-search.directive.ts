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

import {AfterContentChecked, Directive, ElementRef, Input, OnDestroy, OnInit} from '@angular/core';
import {SearchQueryService} from "../services/search-query.service";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";

/**
 * Use this directive to highlight current elastic search terms within an element's text content.
 *
 * @author sadatmalik
 */
@Directive({
  selector: '[appHighlightSearch]'
})
export class HighlightSearchDirective implements OnInit, OnDestroy, AfterContentChecked {
  @Input() showHighlightsOnly: boolean = false;
  private destroy$ = new Subject<void>();
  private currentSearchTerms = [];
  private lastHighlight = '';

  constructor(
    private el: ElementRef,
    private searchQueryService: SearchQueryService
  ) {}

  ngOnInit(): void {
    this.searchQueryService.currentSearchTerms$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(terms => {
      this.currentSearchTerms = terms;
      this.removeHighlighting(); // Clear existing highlights when search term changes
      this.applyHighlighting();
    });
  }

  ngAfterContentChecked(): void {
    const currentContent = this.el.nativeElement.innerHTML;
    if (this.lastHighlight !== currentContent) {
      this.lastHighlight = currentContent;
      this.applyHighlighting();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private removeHighlighting(): void {
    const highlightedElements = this.el.nativeElement.querySelectorAll('.highlight');
    highlightedElements.forEach((node: Node) => {
      const parent = node.parentNode;
      parent.replaceChild(document.createTextNode(node.textContent), node);
      parent.normalize();
    });
  }

  private applyHighlighting(): void {
    if (!this.currentSearchTerms || this.currentSearchTerms.length === 0) {
      return;
    }

    const regex = this.createTermsRegex(this.currentSearchTerms);
    const textNodes = [];
    const walk = document.createTreeWalker(this.el.nativeElement, NodeFilter.SHOW_TEXT, null);
    let node: Node;

    // First, collect all text nodes that contain the term
    while (node = walk.nextNode()) {
      if (regex.test(node.nodeValue)) {
        textNodes.push(node);
      }
      regex.lastIndex = 0; // Reset regex lastIndex
    }

    // Then, process each text node for highlighting
    textNodes.forEach((textNode) => {
      this.highlightInTextNode(textNode, regex);
    });
  }

  private createTermsRegex(searchTerms: string[]): RegExp {
    // Escape special regex characters in terms then join them with '|' to match any of them
    const termsRegex = searchTerms.map(term =>
        term.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&')
    ).join('|');

    // Return a regex that finds any of the terms
    return new RegExp(`(${termsRegex})`, 'gi');
  }

  private highlightInTextNode(node: Node, regex: RegExp): void {
    if (node.parentNode && (node.parentNode as Element).classList.contains('highlight')) {
      return; // Skip this node, as it's already part of a highlighted element
    }

    const docFrag = document.createDocumentFragment();
    let text = node.nodeValue;
    let startIndex = 0;
    let match: RegExpExecArray;

    const matches: RegExpExecArray[] = [];

    try {
      // Process matches in the text node
      while ((match = regex.exec(text))) {

        matches.push(match);

        if (!this.showHighlightsOnly) {
          //Add text before the match
          if (match.index > startIndex) {
            const beforeMatch = text.slice(startIndex, match.index);
            docFrag.appendChild(document.createTextNode(beforeMatch));
          }
        }

        // Create and add a highlighted span for the match
        const highlightSpan = document.createElement('span');
        highlightSpan.className = 'highlight';
        highlightSpan.textContent = match[0];
        docFrag.appendChild(highlightSpan);

        // Update startIndex to the end of the current match
        startIndex = match.index + match[0].length;
      }

      if (!this.showHighlightsOnly) {
        // Add any remaining text after the last match
        if (startIndex < text.length) {
          const afterLastMatch = text.slice(startIndex);
          docFrag.appendChild(document.createTextNode(afterLastMatch));
        }
      }

      // Replace the original text node with the new content if there was a match
      if (docFrag.hasChildNodes()) {
        node.parentNode.replaceChild(docFrag, node);
      }

      console.log('matches', matches.length);

    } catch (error) {
      console.error('Failed to highlight search term: ', error);
    }
  }

}
