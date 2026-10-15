// Prepara a pasta www/ que o Capacitor empacota dentro da APK:
//   - index.html (o MESMO ficheiro da PWA)
//   - native.js  (ponte para os plugins nativos)
//   - jspdf.umd.min.js (cópia local, para não depender da CDN)
import { build } from 'esbuild';
import { copyFileSync, existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';

const CDN_JSPDF = 'https://cdnjs.cloudflare.com/ajax/libs/jspdf/3.0.3/jspdf.umd.min.js';

rmSync('www', { recursive: true, force: true });
mkdirSync('www', { recursive: true });

// 1) index.html — a única alteração é apontar o jsPDF para a cópia local
let html = readFileSync('index.html', 'utf8');
const jspdfLocal = 'node_modules/jspdf/dist/jspdf.umd.min.js';
if (html.includes(CDN_JSPDF) && existsSync(jspdfLocal)) {
  copyFileSync(jspdfLocal, 'www/jspdf.umd.min.js');
  html = html.split(CDN_JSPDF).join('jspdf.umd.min.js');
  console.log('jsPDF: cópia local usada na APK');
} else {
  console.log('jsPDF: a manter a CDN (referência não encontrada ou ficheiro local em falta)');
}
writeFileSync('www/index.html', html);

// 2) ponte nativa
await build({
  entryPoints: ['native/entry.js'],
  bundle: true,
  format: 'iife',
  minify: true,
  target: ['es2019'],
  outfile: 'www/native.js',
  logLevel: 'info',
});

console.log('www/ pronto.');
