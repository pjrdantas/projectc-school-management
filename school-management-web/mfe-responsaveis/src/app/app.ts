import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('mfe-responsaveis');
  private lockedUrl = window.location.href;
  private readonly onPopState = () => {
    window.history.pushState({ navigationLocked: true }, '', this.lockedUrl);
  };

  ngOnInit(): void {
    this.activateNavigationLock();
  }

  ngOnDestroy(): void {
    window.removeEventListener('popstate', this.onPopState);
  }

  private activateNavigationLock() {
    this.lockedUrl = window.location.href;
    window.history.pushState({ navigationLocked: true }, '', this.lockedUrl);
    window.addEventListener('popstate', this.onPopState);
  }
}
