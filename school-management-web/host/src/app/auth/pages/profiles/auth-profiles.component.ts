import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { AccessAdminService } from '../../services/access-admin.service';

@Component({selector:'app-auth-profiles',standalone:true,imports:[CommonModule,ReactiveFormsModule,MatCardModule,MatButtonModule,MatInputModule],templateUrl:'./auth-profiles.component.html'})
export class AuthProfilesComponent implements OnInit{ private service=inject(AccessAdminService); private fb=inject(FormBuilder); perfis:any[]=[]; form=this.fb.group({codigo:['',Validators.required],nome:['',Validators.required],descricao:['']}); ngOnInit(){this.carregar();} carregar(){this.service.listarPerfis().subscribe(r=>this.perfis=r as any[]);} salvar(){if(this.form.invalid)return; this.service.criarPerfil(this.form.value as any).subscribe(()=>{this.form.reset();this.carregar();});}}
