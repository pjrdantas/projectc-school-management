import {
  Directive,
  Input,
  OnDestroy,
  TemplateRef,
  ViewContainerRef,
  inject,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';

@Directive({
  selector: '[appHasPermission]',
  standalone: true,
})
export class HasPermissionDirective implements OnDestroy {
  private shellContext = inject(ShellContextService);
  private templateRef = inject(TemplateRef<unknown>);
  private vcr = inject(ViewContainerRef);
  private sub?: Subscription;

  private currentPermission?: string;

  @Input() set appHasPermission(permission: string) {
    this.currentPermission = permission;
    this.trackPermissionChanges();
  }

  private trackPermissionChanges() {
    this.sub?.unsubscribe();
    this.updateView();
  }

  private updateView() {
    const canRender = this.currentPermission
      ? this.shellContext.hasPermission(this.currentPermission)
      : false;

    if (canRender) {
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
