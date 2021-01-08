import { Component, OnInit } from '@angular/core';
import { AppConfigService } from '../app-config.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {
  gitURL = ""

  constructor(private config: AppConfigService) { }

  ngOnInit(): void {
    this.gitURL = this.config.gitURL
  }

}
