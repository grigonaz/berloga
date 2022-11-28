import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { DatePipe } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { environment } from '../environments/environment';

//Primeng
import { ButtonModule } from 'primeng/button';
import { PanelModule } from 'primeng/panel';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MultiSelectModule } from 'primeng/multiselect';
import { VirtualScrollerModule} from 'primeng/virtualscroller';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {FullCalendarModule} from 'primeng/fullcalendar';
import {CascadeSelectModule} from 'primeng/cascadeselect';
import {AutoCompleteModule} from 'primeng/autocomplete';
import {FileUploadModule} from 'primeng/fileupload';
import { LMarkdownEditorModule } from 'ngx-markdown-editor';

// our components
import { BerlogaMainPageComponent } from './components/pages/berloga-main-page/berloga-main-page.component';
import { BerlogaLoginComponent } from './components/pages/berloga-login/berloga-login.component';
import { BerlogaForumComponent } from './components/pages/berloga-forum/berloga-forum.component';
import { BerlogaDefaultComponent } from './components/pages/berloga-default/berloga-default.component';
import { BerlogaWelcomeComponent } from './components/pages/berloga-welcome/berloga-welcome.component';
import { BerlogaSubjectComponent } from './components/pages/berloga-subject/berloga-subject.component';
import { BerlogaAccountComponent } from './components/pages/berloga-account/berloga-account.component';
import { SubjectListItemComponent } from './components/ui/subject-list-item/subject-list-item.component';
import { BarMenuButtonComponent } from './components/ui/bar-menu-button/bar-menu-button.component';
import { TestBerlogaComponent } from './components/ui/test-berloga/test-berloga.component';
import { NotificationListItemComponent } from './components/ui/notification-list-item/notification-list-item.component';
import { SearchComponent } from './components/ui/search/search.component';
import { SubjectPageComponent } from './components/ui/subject-page/subject-page.component';
import { BerlogaChatComponent } from './components/pages/berloga-chat/berloga-chat.component';

//angular material
import {MatAutocompleteModule} from '@angular/material/autocomplete';

// generated service
import { ApiModule, BASE_PATH } from '@anona/berloga-api-client';
import { FormsModule } from '@angular/forms';
import { BerlogaForumItemComponent } from './components/pages/berloga-forum-item/berloga-forum-item.component';

//ejs-schedule
import {
  ScheduleModule,
  DayService,
  WeekService,
  RecurrenceEditorModule,
  WorkWeekService,
  MonthService,
  MonthAgendaService,
} from '@syncfusion/ej2-angular-schedule';

@NgModule({
  declarations: [
    AppComponent,
    BerlogaMainPageComponent,
    BerlogaLoginComponent,
    BerlogaDefaultComponent,
    BerlogaWelcomeComponent,
    BerlogaSubjectComponent,
    BerlogaAccountComponent,
    BerlogaChatComponent,
    BarMenuButtonComponent,
    TestBerlogaComponent,
    SubjectListItemComponent,
    NotificationListItemComponent,
    SearchComponent,
    SubjectPageComponent,
    BerlogaChatComponent,
    BerlogaForumComponent,
    BerlogaForumItemComponent,
  ],
  imports: [
    BrowserModule,
    ScheduleModule,
    RecurrenceEditorModule,
    AppRoutingModule,
    ButtonModule,
    PanelModule,
    BrowserAnimationsModule,
    InputTextModule,
    ApiModule,
    HttpClientModule,
    FormsModule,
    FileUploadModule,
    DialogModule,
    TableModule,
    ToastModule,
    InputTextareaModule,
    MultiSelectModule,
    VirtualScrollerModule,
    ConfirmDialogModule,
    FullCalendarModule,
    CascadeSelectModule,
    AutoCompleteModule,
    LMarkdownEditorModule
  ],
  providers: [
    { provide: BASE_PATH, useValue: environment.API_PATH },
    DayService,
    WeekService,
    WorkWeekService,
    MonthService,
    DatePipe,
    MonthAgendaService
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
