import { Component, signal, OnInit } from '@angular/core';
import { HomeComponent } from "./pages/home/home.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [HomeComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  protected readonly title = signal('microfrontend');

  ngOnInit() {
    window.addEventListener('storage', (event) => {
      if (event.key === 'token' && !event.newValue) {
        window.location.href = '/login';
      }
    });
  }
}
