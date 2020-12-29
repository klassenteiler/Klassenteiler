import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CreateComponent } from './create/create.component';
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
  {path: '**', component: NotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
