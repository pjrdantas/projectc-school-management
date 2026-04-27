import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  NavigationEnd,
  NavigationStart,
  Router,
  RouterOutlet,
} from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `<router-outlet></router-outlet>`,
})
export class AppComponent implements OnInit, OnDestroy {
  private lockedUrl = window.location.href;
  private isPopStateNavigation = false;
  private routerEventsSubscription?: { unsubscribe: () => void };
  private readonly onPopState = () => {
    this.pushLockedState();
    this.pushLockedState();
    const target = this.lockedUrl.replace(window.location.origin, '');
    this.router.navigateByUrl(target, { replaceUrl: true });
  };

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.activateNavigationLock();
  }

  ngOnDestroy(): void {
    window.removeEventListener('popstate', this.onPopState);
    this.routerEventsSubscription?.unsubscribe();
  }

  private activateNavigationLock() {
    this.lockedUrl = window.location.href;
    this.routerEventsSubscription = this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        this.isPopStateNavigation = event.navigationTrigger === 'popstate';
      }

      if (event instanceof NavigationEnd) {
        if (!this.isPopStateNavigation) {
          this.lockedUrl = window.location.href;
          this.pushLockedState();
        }

        this.isPopStateNavigation = false;
      }
    });

    this.pushLockedState();
    this.pushLockedState();
    window.addEventListener('popstate', this.onPopState);
  }

  private pushLockedState() {
    window.history.pushState({ navigationLocked: true }, '', this.lockedUrl);
  }
}
