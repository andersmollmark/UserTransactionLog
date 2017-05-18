import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {AppComponent} from "./app.component";
import {DataFilterPipe} from "./data-filter.pipe";
import {UtlsFileService} from "./utls-file.service";
import {DataTableModule} from "angular2-datatable";
import {HttpModule} from "@angular/http";
import {FormsModule} from "@angular/forms";
import {DtoSortPipe} from "./dtoSort.pipe";
import {TimeFilterService} from "./timefilter.service";
import {DatepickerModule, TimepickerModule} from "ng2-bootstrap";
import {TimefilterComponent} from "./timefilter.component";
import {ColumnSortPipe} from "./columnSort.pipe";
import {WebsocketService} from "./websocket.service";
import {UtlserverService} from "./utlserver.service";
import {UtlSettingsComponent} from "./utlSettings.component";
import {CryptoService} from "./crypto.service";
import {FetchLogComponent} from "./fetchLog.component";

@NgModule({
    imports:      [ BrowserModule, FormsModule,
        DataTableModule, HttpModule, DatepickerModule.forRoot(), TimepickerModule.forRoot()],
    declarations: [ AppComponent, TimefilterComponent, DataFilterPipe, DtoSortPipe,
        ColumnSortPipe, UtlSettingsComponent, FetchLogComponent ],
    providers: [ UtlsFileService, TimeFilterService, WebsocketService, UtlserverService, CryptoService],
    bootstrap:    [ AppComponent ]
})

export class AppModule { }