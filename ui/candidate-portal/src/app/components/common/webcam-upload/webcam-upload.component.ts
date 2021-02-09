import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {WebcamImage, WebcamInitError, WebcamUtil} from 'ngx-webcam';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";


@Component({
  selector: 'app-webcam-upload',
  templateUrl: './webcam-upload.component.html',
  styleUrls: ['./webcam-upload.component.scss']
})
export class WebcamUploadComponent implements OnInit {

  @Output()
  public pictureTaken = new EventEmitter<WebcamImage>();

  public webcamImage: WebcamImage = null;

// toggle webcam on/off
  public showWebcam = true;
  public allowCameraSwitch = true;
  public multipleWebcamsAvailable = false;
  public deviceId: string;
  public videoOptions: MediaTrackConstraints = {
// width: {ideal: 1024},
// height: {ideal: 576}
  };
  public errors: WebcamInitError[] = [];

// webcam snapshot trigger
  private trigger: Subject<void> = new Subject<void>();

// switch to next / previous / specific webcam; true/false: forward/backwards, string: deviceId
  private nextWebcam: Subject<boolean|string> = new Subject<boolean|string>();

  deleting: boolean;

  constructor(private activeModal: NgbActiveModal) { }

  public ngOnInit(): void {
    WebcamUtil.getAvailableVideoInputs()
      .then((mediaDevices: MediaDeviceInfo[]) => {
        this.multipleWebcamsAvailable = mediaDevices && mediaDevices.length > 1;
      });
  }

  public triggerSnapshot(): void {
    this.trigger.next();
  }

  public toggleWebcam(): void {
    this.showWebcam = !this.showWebcam;
  }

  public handleInitError(error: WebcamInitError): void {
    this.errors.push(error);
  }

  public showNextWebcam(directionOrDeviceId: boolean|string): void {
// true => move forward through devices
// false => move backwards through devices
// string => move to device with given deviceId
    this.nextWebcam.next(directionOrDeviceId);
  }

  public handleImage(webcamImage: WebcamImage): void {
    console.info('received webcam image', webcamImage);
    this.webcamImage = webcamImage;
    this.pictureTaken.emit(webcamImage);
  }

  public cameraWasSwitched(deviceId: string): void {
    console.log('active device: ' + deviceId);
    this.deviceId = deviceId;
  }

  public get triggerObservable(): Observable<void> {
    return this.trigger.asObservable();
  }

  public get nextWebcamObservable(): Observable<boolean|string> {
    return this.nextWebcam.asObservable();
  }


  cancel() {
    this.activeModal.dismiss(false);
  }

  confirm() {
    this.deleting = true;
    this.activeModal.close(true);
  }
}
