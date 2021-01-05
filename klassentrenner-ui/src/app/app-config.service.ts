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

    get configDescription(): string{
      this.check();
      return this.appConfig.description
    }

    get appConfigFile(){
      this.check();
      return this.appConfig
    }

    get classSecretLength():number{
      this.check();
      return this.appConfig.classSecretLength
    }
    get teacherPasswordLength():number{
      this.check();
      return this.appConfig.teacherPasswordLength
    }

    get frontendUrl() {
      this.check()
      const url: string = this.appConfig.frontendUrl;
      if (url.startsWith("http")) {
        throw Error('Froentend URL in config should not start with http..');
      }
  
      return this.appConfig.frontendUrl;
    }

    get news(): string{
      this.check()
      return this.appConfig.news
    }

    get skipMerging(): boolean{
      this.check()

      return this.appConfig.skipMerging
    }
  }