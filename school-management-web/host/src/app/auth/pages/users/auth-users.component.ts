import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
@Component({
  selector: 'app-auth-users',
  standalone: true,
  imports: [ MatCardModule],
  templateUrl: './auth-users.component.html',
  styleUrls: ['./auth-users.component.scss'],
})
export class AuthUsersComponent {}
