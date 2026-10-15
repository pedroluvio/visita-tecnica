// Ponte entre a página (index.html) e os plugins nativos do Capacitor.
// É empacotado em www/native.js e só é carregado quando a página corre dentro da APK.
import { Capacitor, registerPlugin } from '@capacitor/core';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { App } from '@capacitor/app';
import { Filesystem, Directory } from '@capacitor/filesystem';
import { Share } from '@capacitor/share';

// Plugin local: android/app/src/main/java/pt/luvio/visitatecnica/DriveAuthPlugin.java
const DriveAuth = registerPlugin('DriveAuth');

window.NativeAPI = {
  Capacitor,
  Camera, CameraResultType, CameraSource,
  App,
  Filesystem, Directory,
  Share,
  DriveAuth,
};
