import { Component, OnInit } from '@angular/core';

class Contributor{
  constructor(public name: string, public text:string, public links: Link[]){}
}

class Link{
  constructor(public href:string, public icon: string, public hint: string){}
}


const contributors: Contributor[] = [
  new Contributor("Clemens Hutter", "studiert MSc Data Science an der ETH Zürich", 
    [new Link('https://github.com/rauwuckl', 'github', 'GitHub'), 
    new Link("mailto:mail@clemenshutter.de", 'envelope', 'Email'), 
    new Link("https:clemenshutter.de", 'window-sidebar', 'Website'),
    new Link("https://twitter.com/clemens_hutter", "twitter", "Twitter")
  ]),
  new Contributor("Joshua Wiebe", "studiert M.Edu. (Informatik und Mathematik) an der FU Berlin", [
    new Link('mailto:joshuaw29@zedat.fu-berlin.de', 'envelope', 'Email')
  ]),
  new Contributor("Anton Laukemper", "frisch abgeschlossener MSc in Data Science an der Uni Groningen, mit Spezialisierung in Computational Social Science", [
    new Link('https://www.linkedin.com/in/anton-laukemper-a0b98a151', 'linkedin', 'LinkedIn'),
    new Link('mailto:anton@laukemper.it', 'envelope', 'Email')
  ])
]

@Component({
  selector: 'app-about-us',
  templateUrl: './about-us.component.html',
  styleUrls: ['./about-us.component.css']
})
export class AboutUsComponent implements OnInit {

  sortedContributors!: Contributor[];

  constructor() { }

  ngOnInit(): void {
    this.sortedContributors = contributors
      .map((a) => ({sort: Math.random(), value: a}))
      .sort((a, b) => a.sort - b.sort)
      .map((a) => a.value)
  }

}
