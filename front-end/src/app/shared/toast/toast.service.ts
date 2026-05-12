import { Injectable } from '@angular/core';import { BehaviorSubject } from 'rxjs';
export interface ToastMessage{text:string;type:'success'|'error'|'info'}
@Injectable({providedIn:'root'}) export class ToastService{private subject=new BehaviorSubject<ToastMessage[]>([]);toasts$=this.subject.asObservable();show(text:string,type:'success'|'error'|'info'='info'){this.subject.next([...this.subject.value,{text,type}]);setTimeout(()=>this.subject.next(this.subject.value.slice(1)),3500)}success(t:string){this.show(t,'success')}error(t:string){this.show(t,'error')}info(t:string){this.show(t,'info')}}
