import { Directive, Input, TemplateRef, ViewContainerRef, inject, OnDestroy } from '@angular/core';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { Subscription } from 'rxjs';

@Directive({
  selector: '[appHasPermission]',
  standalone: true
})
export class HasPermissionDirective implements OnDestroy {
  private authState = inject(AuthStateService);
  private templateRef = inject(TemplateRef<any>);
  private vcr = inject(ViewContainerRef);
  private sub?: Subscription;

  private currentPermission?: string;

  @Input() set appHasPermission(permission: string) {
    this.currentPermission = permission;
    this.trackPermissionChanges();
  }

  private trackPermissionChanges() {
    this.sub?.unsubscribe();
    // Escuta o usuário. Se ele logar ou mudar, o menu atualiza sozinho.
    this.sub = this.authState.usuario$.subscribe(() => {
      this.updateView();
    });
  }

  private updateView() {
    const userPermissions = this.authState.getPermissions();

    const hasPermission = this.currentPermission
      ? userPermissions.includes(this.currentPermission)
      : false;

    if (hasPermission) {
      if (this.vcr.length === 0) {
        this.vcr.createEmbeddedView(this.templateRef);
      }
    } else {
      this.vcr.clear();
    }
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }
}
