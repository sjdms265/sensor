import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import { Observable, of } from 'rxjs';
import {GraphSensorEndpoint} from "./dto/GraphSensorEndpoint";

@Injectable({
  providedIn: 'root'
})
export class SensorDataService {
  private apiUrl = 'http://localhost:8090/sensorai/graphline/{userId}/{sensorId}';

  constructor(private http: HttpClient) { }

  getSensorDataByUserIdAndSensorId(
    token: string,
    userId: string,
    sensorId: string
  ): Observable<GraphSensorEndpoint[]> {

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    let url = this.apiUrl.replace('{userId}', userId).replace('{sensorId}', sensorId);
    return this.http.get<GraphSensorEndpoint[]>(url, { headers });

  }
}
