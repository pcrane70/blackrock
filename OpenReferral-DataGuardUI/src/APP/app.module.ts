import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { AppComponent } from './app.component';
import { TreeNode } from './recursive.component';
import { BrowserAnimationsModule,NoopAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SearchService } from './provider/search-service';
import { Routes, RouterModule } from '@angular/router';
import { MdExpansionModule,MdIconModule,MdFormFieldModule,MdDatepickerModule,StyleModule,MdInputModule,MdButtonModule,MdSidenavModule,MdCardModule } from '@angular/material';



@NgModule({
  declarations: [
    AppComponent,TreeNode

  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    CommonModule,
    HttpModule,
    ReactiveFormsModule,
    MdExpansionModule,
    NoopAnimationsModule,
    MdIconModule,MdDatepickerModule,StyleModule,MdInputModule,MdButtonModule,MdSidenavModule,MdCardModule
,
  ],
  providers: [SearchService],
  bootstrap: [AppComponent]
})
export class AppModule { }
