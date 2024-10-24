import { Injectable } from '@angular/core';

/**
 * A service that provides a centralised place to manage external links for different categories
 * and languages. It will support retrieving URLs for various purposes (such as eligibility
 * information) based on the current language. If a link for a specific language is not available,
 * it falls back to the English version by default.
 *
 * Example:
 * ```typescript
 * const eligibilityLink = externalLinkService.getLink('eligibility', 'en');
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class ExternalLinkService {

  private links = {
    eligibility: {
      en: 'https://www.talentbeyondboundaries.org/talentcatalog/tbb-eligibility?lang=en',
      ar: 'https://www.talentbeyondboundaries.org/talentcatalog/tbb-eligibility?lang=ar',
      es: 'https://www.talentbeyondboundaries.org/talentcatalog/tbb-eligibility?lang=es',
    }
    // More categories of links can be added here in the future
  };

  constructor() { }

  getLink(category: string, lang: string): string {
    const categoryLinks = this.links[category];
    if (!categoryLinks) {
      console.warn(`Category ${category} not found.`);
      return '';
    }

    // Return the link based on the language, fallback to English if language not found
    return categoryLinks[lang] || categoryLinks['en'] || '';
  }

}
