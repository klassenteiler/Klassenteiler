import { Component, OnInit } from '@angular/core';
import { AppConfigService } from '../app-config.service';
import { environment } from "../../environments/environment";

@Component({
  selector: 'app-index',
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.css']
})
export class IndexComponent implements OnInit {

  configFile: string =""

  constructor(private config : AppConfigService ) { }

  ngOnInit(): void {
    this.configFile = environment.configPath;
  }

}
