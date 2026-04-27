import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { HomeComponent } from './pages/home/home.component';

@Component({
  selector: 'app-root',
  imports: [HomeComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('microfrontend');
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
