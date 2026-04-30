import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AccessAdminService } from '../../../services/access-admin.service';
import { User } from '../../../models/user.model';
@Component({standalone:true,selector:'app-auth-users-list',imports:[CommonModule,RouterLink,MatButtonModule,MatCardModule,MatIconModule],templateUrl:'./auth-users-list.component.html'})
export class AuthUsersListComponent implements OnInit{ private service=inject(AccessAdminService); private router=inject(Router); usuarios:User[]=[]; ngOnInit(){this.service.listarUsuarios().subscribe(r=>this.usuarios=r);} novo(){this.router.navigate(['/auth/users/new']);} excluir(id:string){this.service.excluirUsuario(id).subscribe(()=>this.ngOnInit());}}
