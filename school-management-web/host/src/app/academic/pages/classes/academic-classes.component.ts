import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
@Component({
  selector: 'app-academic-classes',
  standalone: true,
  imports: [ MatCardModule],
  templateUrl: './academic-classes.component.html',
  styleUrls: ['./academic-classes.component.scss'],
})
export class AcademicClassesComponent {}
