import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AboutUsComponent } from './about-us/about-us.component';
import { CreateComponent } from './create/create.component';
import { DatenschutzComponent } from './datenschutz/datenschutz.component';
import { ImpressumComponent } from './impressum/impressum.component';
import { IndexComponent } from './index/index.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { StudentViewComponent } from './student-view/student-view.component';
import { TeacherViewComponent } from './teacher-view/teacher-view.component';
import { SchoolClassResolver } from './_resolvers/school-class.resolver';

const routes: Routes = [
  {path: '', component: IndexComponent},
  {path: 'create', component: CreateComponent},
  {path: 'teacher/:id/:classSecret', component: TeacherViewComponent, resolve: {schoolClass: SchoolClassResolver}},
  {path: 'student/:id/:classSecret', component: StudentViewComponent, resolve: {schoolClass: SchoolClassResolver}},
  {path: 'notFound', component: NotFoundComponent},
  {path: 'impressum', component: ImpressumComponent},
  {path: 'datenschutz', component: DatenschutzComponent},
  {path: 'about', component: AboutUsComponent},
  {path: '**', component: NotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    scrollOffset: [0, 64],
    // anchorScrolling: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
