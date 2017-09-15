import { Component, ViewChild, ElementRef, Renderer2, Inject, ChangeDetectorRef } from '@angular/core';
import { FormControl, NgModel, } from '@angular/forms';
import { SearchService } from './provider/search-service';
import 'rxjs/Rx';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/startWith';
//import { Location } from './provider/portfolio';
import { KeyValue } from './provider/key-value';
import { DOCUMENT } from '@angular/platform-browser';
import { environment } from '../environments/environment';
@Component({
  selector: 'conflit-results',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {

  private parentKeyValue: KeyValue[] = [];
  private keyValues: KeyValue[] = [];

  private conflicts:string[] =[];

  constructor(private searchService: SearchService) {

  }
  ngOnInit() {
    this.searchService.getConflictsByEntityID("IRMA-FIRST Responders").subscribe(item => {
      this.keyValues = this.toMap(item);

    })

     this.searchService.getConflicts().subscribe(item => {
      this.conflicts = item;

    })

  }



  public toMap(object: any): KeyValue[] {
    let keyValues: KeyValue[] = [];
    for (let key in object) {
      let keyValue = new KeyValue();
      let value = object[key];
      if (object[key] instanceof Array) {
        value = this.toList(object[key], key);
      } else if (object[key] instanceof Object) {
        value = this.toMap(object[key]);
      }
      keyValue.key = key;
      keyValue.value = value;
      keyValues.push(keyValue);
    }

    return keyValues;
  }

  public toList(object: any, key: any): KeyValue[] {
    let keyValues: KeyValue[] = [];
    let count = 0;
    for (let item of object) {
      let value;
      let keyValue = new KeyValue();
      if (item instanceof Array) {
        value = this.toList(item, key);
      }
      else if (item instanceof Object) {
        value = this.toMap(item);
      }
      count++
      keyValue.key = key + "[" + count + "]";
      keyValue.value = value;
      keyValues.push(keyValue);
    }
    return keyValues;
  }

  navigate(entity:string){
    this.searchService.getConflictsByEntityID(entity).subscribe(item => {
      this.keyValues = this.toMap(item);

    })
  }
}
