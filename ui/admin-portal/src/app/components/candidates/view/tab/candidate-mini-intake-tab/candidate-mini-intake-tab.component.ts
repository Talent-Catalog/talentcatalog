import {Component} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';

import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

@Component({
  selector: 'app-candidate-mini-intake-tab',
  templateUrl: './candidate-mini-intake-tab.component.html',
  styleUrls: ['./candidate-mini-intake-tab.component.scss']
})
export class CandidateMiniIntakeTabComponent extends IntakeComponentTabBase {
  exportAsPdf() {
      // parent div is the html element which has to be converted to PDF
      html2canvas(document.querySelector("#MiniIntake-panel")).then(canvas => {

        const pdf = new jsPDF('p', 'pt', [canvas.width, canvas.height]);

        const imgData  = canvas.toDataURL("image/jpeg", 1.0);
        pdf.addImage(imgData, 0, 0, canvas.width, canvas.height);
        pdf.save('MiniIntake_' + this.candidate.user.firstName + '_' + this.candidate.user.lastName + '.pdf');
      });
    }

}
