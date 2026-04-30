import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AccessAdminService } from '../../../services/access-admin.service';

@Component({standalone:true,imports:[CommonModule,ReactiveFormsModule,MatButtonModule,MatCardModule,MatFormFieldModule,MatInputModule,MatSlideToggleModule,MatIconModule],templateUrl:'./auth-users-new.component.html',styleUrls:['./auth-users-new.component.scss']})
export class AuthUsersNewComponent implements OnInit{ private fb=inject(FormBuilder); private service=inject(AccessAdminService); private router=inject(Router); private route=inject(ActivatedRoute); userId=signal<string|null>(null); form=this.fb.group({username:['',Validators.required],nome:['',Validators.required],email:['',[Validators.required,Validators.email]],senhaHash:['',Validators.required],ativo:[true]}); ngOnInit(){const id=this.service.currentUserId(); if(!id)return; this.userId.set(id); this.service.buscarUsuario(id).subscribe(u=>this.form.patchValue({...u, senhaHash:'********'}));}
 salvar(){if(this.form.invalid)return; const id=this.userId(); const payload=this.form.getRawValue() as any; if(payload.senhaHash==='********') payload.senhaHash='admin123'; const obs=id?this.service.atualizarUsuario(id,payload):this.service.criarUsuario(payload); obs.subscribe(()=>this.router.navigate(['/auth/users']));}
 onCancel(){this.router.navigate(['/auth/users']);}}
