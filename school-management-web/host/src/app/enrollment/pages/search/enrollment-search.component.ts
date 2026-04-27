import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
@Component({
  selector: 'app-enrollment-search',
  standalone: true,
  imports: [ MatCardModule],
  templateUrl: './enrollment-search.component.html',
  styleUrls: ['./enrollment-search.component.scss'],
})
export class EnrollmentSearchComponent {}
