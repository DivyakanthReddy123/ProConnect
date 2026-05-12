import { Component, Input } from '@angular/core';
@Component({selector:'app-avatar',templateUrl:'./avatar.component.html',styleUrls:['./avatar.component.css']})
export class AvatarComponent{ @Input() src?: string|null; @Input() name=''; @Input() size=44; get initials(){const p=(this.name||'User').trim().split(/\s+/).filter(Boolean);return p.slice(0,2).map(x=>x.charAt(0).toUpperCase()).join('')||'U'} get bg(){const c=['#E8F2FF','#EEF2FF','#ECFDF5','#F1F5F9','#F8FAFC','#F1F1ED'];const s=(this.name||'User').split('').reduce((a,ch)=>a+ch.charCodeAt(0),0);return c[s%c.length]}}
