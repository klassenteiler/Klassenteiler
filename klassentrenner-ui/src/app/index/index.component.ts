import { Component, OnInit } from '@angular/core';
import { AppConfigService } from '../app-config.service';
import { environment } from "../../environments/environment";
import { PdfTools } from '../_tools/pdf-tools';

@Component({
  selector: 'app-index',
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.css']
})
export class IndexComponent implements OnInit {

  configFileDescription: string =""

  news: string = ""

  constructor( private configService: AppConfigService) { }

  ngOnInit(): void {
    this.configFileDescription = this.configService.configDescription;
    this.news = this.configService.news;
    console.log(this.configFileDescription)
  }

  makeTestPDF(){
    PdfTools.teacherSummaryPDF("AMG - 9b", "9abZxy92", "http://localhost:4200/teacher/22/DSGZffv1");
  }
}
