import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
@Component({
  selector: 'app-enrollment-new',
  standalone: true,
    imports: [ MatCardModule],
  templateUrl: './enrollment-new.component.html',
  styleUrls: ['./enrollment-new.component.scss'],
})
export class EnrollmentNewComponent {}
