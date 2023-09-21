import {Component, Input, OnInit} from '@angular/core';
import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import {Candidate} from "../../../model/candidate";

@Component({
  selector: 'app-export-pdf',
  templateUrl: './export-pdf.component.html',
  styleUrls: ['./export-pdf.component.scss']
})
export class ExportPdfComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() idToExport: string;

  saving: boolean;

  constructor() { }

  ngOnInit(): void {
  }

  /**
   * Called when export button on intake forms is clicked. Exports the div containing
   * the forms and downloads the file.
   * @param formName is the id of the div container explain which form relates to.
   */
  public exportAsPdf(formName: string) {
    // parent div is the html element which has to be converted to PDF
    this.saving = true;
    const element = document.getElementById(formName);
    html2canvas(element, {scrollY: -window.scrollY, scale: 1}).then(canvas => {
      const heightRatio = canvas.height / canvas.width;
      //const widthRadio = canvas.width / canvas.height;
      //jsPdf has a max height of 14440 so set the height less than this
      let pdf;
      let height;
      let width;
      if (canvas.height > canvas.width) {
        // Make the PDF the same size as the canvas content
        height = 14000;
        width = height / heightRatio;
        pdf = new jsPDF('p', 'pt', [width, height]);
      } else {
        // Make the PDF a landscape size.
        width = canvas.width / heightRatio;
        height = width * heightRatio;
        pdf = new jsPDF('l', 'pt', [width, height]);
      }
      const imgData  = canvas.toDataURL("image/jpeg", 1.0);
      pdf.addImage(imgData, 0, 0, width, height);
      pdf.save(formName + '_' + this.candidate.user.firstName + '_' + this.candidate.user.lastName + '.pdf');
      this.saving = false;
    })
  };

}
