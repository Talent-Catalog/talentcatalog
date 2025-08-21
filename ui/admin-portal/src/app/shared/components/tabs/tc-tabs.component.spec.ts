import {ComponentFixture, TestBed} from '@angular/core/testing';
import {Component} from '@angular/core';
import {By} from '@angular/platform-browser';
import {TcTabsComponent} from './tc-tabs.component';
import {TcTabComponent} from './tab/tc-tab.component';
import {TcTabHeaderComponent} from "./tab/header/tc-tab-header.component";
import {TcTabContentComponent} from "./tab/content/tc-tab-content.component";

@Component({
  template: `
    <tc-tabs [activeTabId]="activeTabId" (tabChanged)="onTabChanged($event)">
      <tc-tab id="FirstTab" description="First tab description">
        <tc-tab-header>First Tab</tc-tab-header>
        <tc-tab-content>First Tab Content</tc-tab-content>
      </tc-tab>
      <tc-tab id="SecondTab" description="Second tab description">
        <tc-tab-header>Second Tab</tc-tab-header>
        <tc-tab-content>Second Tab Content</tc-tab-content>
      </tc-tab>
    </tc-tabs>
  `
})
class TestHostComponent {
  activeTabId = 'FirstTab';
  lastChanged: string | null = null;
  onTabChanged(tabId: string) {
    this.lastChanged = tabId;
  }
}

describe('TcTabsComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TcTabsComponent, TcTabComponent, TcTabHeaderComponent, TcTabContentComponent, TestHostComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render tab headers in the nav', () => {
    const headers = fixture.debugElement.queryAll(By.css('ul.nav-tabs li a'));
    expect(headers.length).toBe(2);
    expect(headers[0].nativeElement.textContent.trim()).toContain('First Tab');
    expect(headers[1].nativeElement.textContent.trim()).toContain('Second Tab');
  });

  it('should apply active class to the active tab', () => {
    const headers = fixture.debugElement.queryAll(By.css('ul.nav-tabs li a'));
    expect(headers[0].classes['active']).toBeTrue();
    expect(headers[1].classes['active']).toBeFalsy();
  });

  it('should only show content of the active tab', () => {
    const contents = fixture.debugElement.queryAll(By.css('.tab-content div'));
    expect(contents.length).toBe(2);

    // First tab visible
    expect(contents[0].nativeElement.hidden).toBeFalse();
    expect(contents[1].nativeElement.hidden).toBeTrue();
  });

  it('should switch tab when header is clicked', () => {
    fixture.detectChanges();

    // Query all tab header links
    const headers = fixture.debugElement.queryAll(By.css('ul.nav-tabs li a'));

    // Click the second tab header using nativeElement.click()
    headers[1].nativeElement.click();
    fixture.detectChanges();

    // Query all tab content divs
    const contents = fixture.debugElement.queryAll(By.css('.tab-content div'));

    // Verify the first tab is hidden and the second tab is visible
    expect(contents[0].nativeElement.hasAttribute('hidden')).toBeTrue();
    expect(contents[1].nativeElement.hasAttribute('hidden')).toBeFalse();

    // Verify the host component received the correct tab ID
    expect(host.lastChanged).toBe('SecondTab'); // Use the actual tab ID
  });
});
