import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { AccessAdminService } from '../../services/access-admin.service';

@Component({selector:'app-auth-permissions',standalone:true,imports:[CommonModule,ReactiveFormsModule,MatCardModule,MatButtonModule,MatInputModule],templateUrl:'./auth-permissions.component.html'})
export class AuthPermissionsComponent implements OnInit{ private service=inject(AccessAdminService); private fb=inject(FormBuilder); permissoes:any[]=[]; form=this.fb.group({codigo:['',Validators.required],descricao:['']}); ngOnInit(){this.carregar();} carregar(){this.service.listarPermissoes().subscribe(r=>this.permissoes=r as any[]);} salvar(){if(this.form.invalid)return; this.service.criarPermissao(this.form.value as any).subscribe(()=>{this.form.reset();this.carregar();});}}
