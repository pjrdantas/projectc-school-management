import { Injectable } from '@angular/core';

type Environment = 'local' | 'dev' | 'hml' | 'prod';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  private readonly env: Environment = 'local'; // ✅ Alterar conforme necessidade

  private readonly urls: Record<Environment, string> = {
    local: 'http://localhost:8080/api',
    dev: 'https://dev.api.suaempresa.com/api',
    hml: 'https://hml.api.suaempresa.com/api',
    prod: 'https://api.suaempresa.com/api'
  };

  private readonly urlService: string;

  constructor() {
    this.urlService = this.urls[this.env].replace(/\/$/, '');
  }

  getUrlService(): string {
    return this.urlService;
  }
}
