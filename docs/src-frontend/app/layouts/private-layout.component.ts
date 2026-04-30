import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuComponent } from '../pages/menu/menu.component';

@Component({
  selector: 'app-private-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, MenuComponent],
  templateUrl: './private-layout.component.html',
  styleUrls: ['./private-layout.component.scss']
})
export class PrivateLayoutComponent {}
