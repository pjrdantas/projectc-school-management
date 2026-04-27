import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MatCardModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit, OnDestroy {
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
