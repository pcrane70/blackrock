import { Injectable } from '@angular/core';
import { Http, Response, Headers } from '@angular/http';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import 'rxjs/add/operator/toPromise';
import {environment} from '../../environments/environment';


@Injectable()
export class SearchService {
    constructor(private http: Http) { }

    public getConflictsByEntityID(entityId:string): Observable<any[]> {
        console.log("Calling Search");
        let headers: Headers = new Headers();
        var url = environment.baseURL+`conflicts/`+entityId;
        return this.http.get(url)
            .map((res: Response) => {
                console.log()
                return [res.json()];
            })
            .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
    }

     public getConflicts(): Observable<any[]> {
        console.log("Calling Search");
        let headers: Headers = new Headers();
        var url = environment.baseURL+`conflicts`;
        return this.http.get(url)
            .map((res: Response) => {
                console.log()
                return res.json();
            })
            .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
    }

}