import { Component, OnInit } from '@angular/core';
import {SearchUsersComponent} from "./users/search-users.component";
import {SearchNationalitiesComponent} from "./nationalities/search-nationalities.component";
import {SearchCountriesComponent} from "./countries/search-countries.component";
import {SearchLanguagesComponent} from "./languages/search-languages.component";
import {SearchOccupationsComponent} from "./occupations/search-occupations.component";
import {SearchIndustriesComponent} from "./industries/search-industries.component";
import {SearchLanguageLevelsComponent} from "./language-levels/search-language-levels.component";
import {SearchEducationLevelsComponent} from "./education-levels/search-education-levels.component";
import {SearchEducationMajorsComponent} from "./education-majors/search-education-majors.component";

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

  selectedTab;

  public tabs = [
    {
      label: 'Admin Users',
      component: SearchUsersComponent
    },
    {
      label: 'Nationalities',
      component: SearchNationalitiesComponent
    },
    {
      label: 'Countries',
      component: SearchCountriesComponent
    },
    {
      label: 'Languages',
      component: SearchLanguagesComponent
    },
    {
      label: 'Occupations',
      component: SearchOccupationsComponent
    },
    {
      label: 'Industries',
      component: SearchIndustriesComponent
    },
    {
      label: 'Language Levels',
      component: SearchLanguageLevelsComponent
    },
    {
      label: 'Education Levels',
      component: SearchEducationLevelsComponent
    },
    {
      label: 'Education Majors',
      component: SearchEducationMajorsComponent
    }
  ];

  ngOnInit(){
      this.selectedTab = this.tabs[0];
  }

  setSelectedTab(tab){
    console.log('tab', tab);
    this.selectedTab = tab;
  }
}
