import {Directive, ElementRef, OnDestroy, OnInit} from '@angular/core';
import {SearchQueryService} from "../services/search-query.service";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";

/**
 * Use this directive to dynamically highlight current elastic search terms within an element's
 * text content.
 *
 * @author sadatmalik
 */
@Directive({
  selector: '[appHighlightSearch]'
})
export class HighlightSearchDirective implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private currentSearchTerms = [];

  constructor(
    private el: ElementRef,
    private searchQueryService: SearchQueryService
  ) {}

  ngOnInit(): void {
    this.searchQueryService.currentSearchTerms.pipe(
      takeUntil(this.destroy$)
    ).subscribe(terms => {
      this.currentSearchTerms = terms;
      this.applyHighlighting();
    });
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
    this.removeHighlighting(); // Clear existing highlights
    if (!this.currentSearchTerms || this.currentSearchTerms.length === 0) {
      return;
    }

    const regex = this.createTermsRegex(this.currentSearchTerms);
    const textNodes = [];
    const walk = document.createTreeWalker(this.el.nativeElement, NodeFilter.SHOW_TEXT, null);
    let node: Node;

    // First, collect all text nodes
    while (node = walk.nextNode()) {
      textNodes.push(node);
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
    // Return a regex to find any of the terms
    return new RegExp(`(${termsRegex})`, 'gi');
  }

  private highlightInTextNode(node: Node, regex: RegExp): void {
    if (!regex.test(node.nodeValue))
      return; // Skip nodes that don't contain the term

    const docFrag = document.createDocumentFragment();
    let text = node.nodeValue;
    let startIndex = 0;
    let match: RegExpExecArray;

    // Reset the lastIndex of the regex to start from the beginning
    regex.lastIndex = 0;

    try {
      // Process matches in the text node
      while ((match = regex.exec(text))) {

        // Add text before the match
        if (match.index > startIndex) {
          const beforeMatch = text.slice(startIndex, match.index);
          docFrag.appendChild(document.createTextNode(beforeMatch));
        }

        // Create and add a highlighted span for the match
        const highlightSpan = document.createElement('span');
        highlightSpan.className = 'highlight';
        highlightSpan.textContent = match[0];
        docFrag.appendChild(highlightSpan);

        // Update startIndex to the end of the current match
        startIndex = match.index + match[0].length;
      }

      // Add any remaining text after the last match
      if (startIndex < text.length) {
        const afterLastMatch = text.slice(startIndex);
        docFrag.appendChild(document.createTextNode(afterLastMatch));
      }

      // Replace the original text node with the new content if there was a match
      if (docFrag.hasChildNodes()) {
        node.parentNode.replaceChild(docFrag, node);
      }

    } catch (error) {
      console.error('Failed to highlight search term: ', error);
    }
  }

}
