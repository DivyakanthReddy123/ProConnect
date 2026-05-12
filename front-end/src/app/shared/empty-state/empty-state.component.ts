import { Component, Input } from '@angular/core';
@Component({selector:'app-empty-state',templateUrl:'./empty-state.component.html',styleUrls:['./empty-state.component.css']})
export class EmptyStateComponent{ @Input() title='Nothing here yet'; @Input() message='When there is data, it will appear here.'; @Input() cta=''; }
