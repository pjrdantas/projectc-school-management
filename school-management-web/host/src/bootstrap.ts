import { enableProdMode, isDevMode } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app';

if (!isDevMode()) {
  enableProdMode();
}

if (!isDevMode()) {
  console.log = () => {};
  console.debug = () => {};
  console.warn = () => {};
  console.info = () => {};
  console.error = () => {};
}

bootstrapApplication(AppComponent, appConfig)
  .catch(err => {
     if (isDevMode()) console.error('Erro no bootstrap:', err);
  });
