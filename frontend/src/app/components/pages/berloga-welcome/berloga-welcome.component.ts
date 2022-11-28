import { Component, NgModule, OnInit, ViewChild } from '@angular/core';
import { NotificationI } from '../../interfaces/NotificationI';
import {
  View,
  EventSettingsModel,
  Schedule,
  PopupOpenEventArgs,
} from '@syncfusion/ej2-angular-schedule';
import { ActionEventArgs } from '@syncfusion/ej2-angular-schedule';
import { RecordDTO } from '@anona/berloga-api-client';
import { DefaultService, SubjectDTO } from '@anona/berloga-api-client';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-berloga-welcome',
  templateUrl: './berloga-welcome.component.html',
  // template:'[eventSettings]="eventObject"',
  styleUrls: ['./berloga-welcome.component.scss'],
})
export class BerlogaWelcomeComponent implements OnInit {
  records: Record<string, any>[];
  recordsDTO: RecordDTO[];

  dataSourcse: any[] = [];

  @ViewChild('scheduleObj') scheduleObj: Schedule;
  @ViewChild('actionEvent') actionEvent: ActionEventArgs;

  eventSettings: EventSettingsModel = {
    dataSource: this.dataSourcse,
  };

  listOfNotifications: NotificationI[] = [];

  onActionComplete() {}

  constructor(private service: DefaultService, public datepipe: DatePipe) {
    this.listOfNotifications.push({
      id: 3,
      type: 'calendar',
    } as NotificationI);
    this.listOfNotifications.push({
      id: 3,
      type: 'calendar',
    } as NotificationI);
  }

  ngOnInit(): void {
    this.getAllEvents();
  }

  getAllEvents() {
    this.service.calendarControllerGetCalendarGET().subscribe((data) => {
      this.recordsDTO = data as RecordDTO[];
      this.dataSourcse = [];
      this.recordsDTO.forEach((element) => {
        let recoredTemp = {
          StartTime: element.dateTimeStart,
          Subject: element.name,
          EndTime: element.dateTimeFinish,
          BerlogaId: element.id,
        };
        this.dataSourcse.push(recoredTemp);
      });
      this.scheduleObj.eventSettings.dataSource = this.dataSourcse;
    });
  }


  createEvent(record: RecordDTO) {
    this.service
      .calendarControllerCreateRecordPOST(record)
      .subscribe((data) => {
        this.getAllEvents();
      });
  }

  editEvent(record: RecordDTO) {
    this.service
      .calendarControllerUpdateRecordPOST(record)
      .subscribe((data) => {
        this.getAllEvents();
      });
  }

  removeEvent(id: any) {
    this.service
      .calendarControllerRemoveRecordDELETE(id)
      .subscribe((data) => {
        this.getAllEvents();
      });

  }

  action(args: ActionEventArgs) {
    console.log(args.requestType);
    switch (args.requestType) {

      case 'eventCreated': {
        this.records = args.data as Record<string, any>[];
        let angular_record: Record<string, any>;
        angular_record = this.records[0];
        let recordTemp = {
          dateTimeFinish: angular_record.EndTime,
          dateTimeStart: angular_record.StartTime,
          name: angular_record.Subject,
        } as RecordDTO;
        this.createEvent(recordTemp);
        break;
      }

      case 'eventChanged': {
        this.records = args.data as Record<string, any>[];
        let angular_record: Record<string, any>;
        angular_record = this.records[0];
        let recordTemp = {
          dateTimeFinish: angular_record.EndTime,
          dateTimeStart: angular_record.StartTime,
          name: angular_record.Subject,
          id : angular_record.BerlogaId
        } as RecordDTO;
        this.editEvent(recordTemp);
        break;
      }

      case 'eventRemoved': {
        this.records = args.data as Record<string, any>[];
        let angular_record: Record<string, any>;
        angular_record = this.records[0];
        let recordTemp = {
          dateTimeFinish: angular_record.EndTime,
          dateTimeStart: angular_record.StartTime,
          name: angular_record.Subject,
          id : angular_record.BerlogaId
        } as RecordDTO;
        this.removeEvent(recordTemp.id);
        break;
      }
    }
  }

}
