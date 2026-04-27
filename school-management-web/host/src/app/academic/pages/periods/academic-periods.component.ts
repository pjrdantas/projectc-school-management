import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
@Component({
  selector: 'app-academic-periods',
  standalone: true,
  imports: [ MatCardModule],
  templateUrl: './academic-periods.component.html',
  styleUrls: ['./academic-periods.component.scss'],
})
export class AcademicPeriodsComponent {}
