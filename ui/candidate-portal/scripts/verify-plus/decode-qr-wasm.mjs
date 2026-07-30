#!/usr/bin/env node
/*
 * Diagnostic script to decode QR content from a clean PNG image using
 * zxing-wasm (C++ ZXing compiled to WebAssembly).
 */

import {existsSync, readFileSync} from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {prepareZXingModule, readBarcodes} from 'zxing-wasm/reader';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const inputPath = process.argv[2]
  ? path.resolve(process.argv[2])
  : path.join(scriptDir, '../../docs/verify-plus/unhcr-sample.png');

function resolveWasmPath() {
  const wasmPath = path.join(scriptDir, '../../node_modules/zxing-wasm/dist/reader/zxing_reader.wasm');
  return existsSync(wasmPath) ? wasmPath : null;
}

async function tryDecode(label, pngBytes, options) {
  try {
    const results = await readBarcodes(pngBytes, options);
    const hit = results.find(result => Boolean(result.text));

    if (hit) {
      console.log(`PASS [${label}] format=${hit.format} len=${hit.text.length}`);
      console.log(hit.text.slice(0, 120));
      return true;
    }

    console.log(`fail [${label}] no symbols (${results.length} results)`);
    return false;
  } catch (error) {
    const name = error && error.name ? error.name : String(error);
    console.log(`fail [${label}] ${name}`);
    return false;
  }
}

async function run() {
  if (!existsSync(inputPath)) {
    console.error(`Input file not found: ${inputPath}`);
    process.exit(2);
  }

  const wasmPath = resolveWasmPath();
  if (!wasmPath) {
    console.error('zxing_reader.wasm not found in node_modules/zxing-wasm/dist/reader');
    process.exit(2);
  }

  await prepareZXingModule({
    overrides: {wasmBinary: readFileSync(wasmPath)},
    fireImmediately: true
  });

  const pngBytes = new Uint8Array(readFileSync(inputPath));
  const baseOptions = {formats: ['QRCode'], maxNumberOfSymbols: 1};

  const results = [
    await tryDecode('default+tryHarder', pngBytes, {...baseOptions, tryHarder: true}),
    await tryDecode('aggressive', pngBytes, {
      ...baseOptions,
      tryHarder: true,
      tryRotate: true,
      tryInvert: true,
      tryDownscale: true
    })
  ];

  if (!results.some(Boolean)) {
    process.exitCode = 1;
  }
}

void run();
