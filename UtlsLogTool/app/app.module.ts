
import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent }   from './app.component';
import { DataFilterPipe } from './data-filter.pipe';
import {StringSortPipe} from './stringSort.pipe';

import {UtlsFileService} from "./utls-file.service";
import {DataTableModule} from "angular2-datatable";
import {HttpModule} from "@angular/http";
import {FormsModule} from "@angular/forms";
import {DtoSortPipe} from "./dtoSort.pipe";
import {DatepickerModule} from "ng2-bootstrap";
import {FilterService} from "./filter.service";
import {TimeStampSortPipe} from "./timestampSort.pipe";

@NgModule({
    imports:      [ BrowserModule, FormsModule,
        DataTableModule, HttpModule, DatepickerModule.forRoot()],
    declarations: [ AppComponent, DataFilterPipe, StringSortPipe, DtoSortPipe, TimeStampSortPipe ],
    providers: [ UtlsFileService, FilterService],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }