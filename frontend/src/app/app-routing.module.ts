import { BerlogaAccountComponent } from './components/pages/berloga-account/berloga-account.component';
import { SubjectPageComponent } from './components/ui/subject-page/subject-page.component';
import { AuthGuardService } from './components/services/AuthGuard.service';
import { BerlogaSubjectComponent } from './components/pages/berloga-subject/berloga-subject.component';
import { BerlogaWelcomeComponent } from './components/pages/berloga-welcome/berloga-welcome.component';
import { BerlogaMainPageComponent } from './components/pages/berloga-main-page/berloga-main-page.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BerlogaLoginComponent } from './components/pages/berloga-login/berloga-login.component';
import { BerlogaDefaultComponent } from './components/pages/berloga-default/berloga-default.component';
import { BerlogaChatComponent } from './components/pages/berloga-chat/berloga-chat.component';
import { BerlogaForumComponent } from './components/pages/berloga-forum/berloga-forum.component';
import { BerlogaForumItemComponent } from './components/pages/berloga-forum-item/berloga-forum-item.component';

const routes: Routes = [
  { path: '', component: BerlogaMainPageComponent },
  { path: 'login', component: BerlogaLoginComponent },
  {
    path: 'portal',
    component: BerlogaDefaultComponent,
    canActivate: [AuthGuardService],
    children: [
      { path: 'welcome', component: BerlogaWelcomeComponent, canActivate: [AuthGuardService] },
      { path: 'subjects', component: BerlogaSubjectComponent, canActivate: [AuthGuardService] },
      { path: 'subject/:id', component: SubjectPageComponent, canActivate: [AuthGuardService] },
      { path: 'chats', component: BerlogaChatComponent, canActivate: [AuthGuardService] },
      { path: 'account', component: BerlogaAccountComponent, canActivate: [AuthGuardService] },
      { path: 'forum', component: BerlogaForumComponent, canActivate: [AuthGuardService] },
      { path: 'question/:id', component: BerlogaForumItemComponent, canActivate: [AuthGuardService] },
      { path: '**', redirectTo: 'welcome' },
    ],
  },
  { path: '**', redirectTo: 'portal/' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
