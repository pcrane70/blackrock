import {Component, forwardRef, Input} from '@angular/core'

@Component({
  selector: 'recursive-node',
  templateUrl: './recursive.component.html',
  styleUrls: ['./recursive.component.css'],
})
export class TreeNode {
  @Input() node;


isArray(val) { 
 if(val instanceof Array)
  return true;
  return false;
  //return typeof val === 'number';
 }

}