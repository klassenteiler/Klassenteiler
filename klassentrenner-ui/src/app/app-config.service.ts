import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../environments/environment"

export class MockAppConfigService {
  apiBaseUrl(){return "http://localhost:5000"}
  fronentUrl(){return "http://localhost:4200"}
}

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

    check(){

  
      if (!this.appConfig) {
        throw Error('Config file not loaded!');
      }
    }
  
    // This is an example property ... you can make it however you want.
    get apiBaseUrl() {
      this.check();
  
      return this.appConfig.apiBaseUrl;
    }

    get maxFriends(): number{
      this.check();
      return this.appConfig.maxFriends;
    }

    get appConfigFile(){
      this.check();
      return this.appConfig
    }

    get frontendUrl() {
      if (!this.appConfig) {
        throw Error('Config file not loaded!');
      }
  
      return this.appConfig.frontendUrl;
    }
  }