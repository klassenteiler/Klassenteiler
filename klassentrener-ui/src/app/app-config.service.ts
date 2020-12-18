import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../environments/environment"

@Injectable({
    providedIn: 'root'
  })
  export class AppConfigService {
  
    private appConfig: any;
  
    constructor(private http: HttpClient) { }
  
    loadAppConfig() {
      return this.http.get(environment.configPath)
        .toPromise()
        .then(data => {
          this.appConfig = data;
        });
    }
  
    // This is an example property ... you can make it however you want.
    get apiBaseUrl() {
  
      if (!this.appConfig) {
        throw Error('Config file not loaded!');
      }
  
      return this.appConfig.apiBaseUrl;
    }
  }