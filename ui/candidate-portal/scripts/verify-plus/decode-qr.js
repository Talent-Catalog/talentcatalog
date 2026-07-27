#!/usr/bin/env node
/*
 * Diagnostic script to decode QR content from a clean PNG image using
 * @zxing/library core classes only (no browser/camera path).
 */

const fs = require('fs');
const path = require('path');
const {PNG} = require('pngjs');
const {
  BinaryBitmap,
  DecodeHintType,
  GlobalHistogramBinarizer,
  HybridBinarizer,
  QRCodeReader,
  RGBLuminanceSource
} = require('@zxing/library');

const defaultFile = path.join(__dirname, '../../docs/verify-plus/unhcr-sample.png');
const inputPath = process.argv[2] ? path.resolve(process.argv[2]) : defaultFile;

function toPackedRgbInt32(rgbaData, width, height) {
  const packed = new Int32Array(width * height);
  for (let i = 0; i < packed.length; i += 1) {
    const offset = i * 4;
    const r = rgbaData[offset];
    const g = rgbaData[offset + 1];
    const b = rgbaData[offset + 2];
    packed[i] = (r << 16) | (g << 8) | b;
  }
  return packed;
}

function decodeVariant(label, packedRgb, width, height, hints, makeBinarizer) {
  try {
    const source = new RGBLuminanceSource(packedRgb, width, height);
    const bitmap = new BinaryBitmap(makeBinarizer(source));
    const result = new QRCodeReader().decode(bitmap, hints);
    const text = result.getText();
    console.log(`PASS [${label}] len=${text.length}`);
    console.log(text.slice(0, 120));
    return true;
  } catch (error) {
    const name = error && error.name ? error.name : String(error);
    console.log(`fail [${label}] ${name}`);
    return false;
  }
}

function run() {
  if (!fs.existsSync(inputPath)) {
    console.error(`Input file not found: ${inputPath}`);
    process.exit(2);
  }

  const png = PNG.sync.read(fs.readFileSync(inputPath));
  const {width, height, data} = png;
  const packedRgb = toPackedRgbInt32(data, width, height);

  console.log(`Image ${width}x${height}`);

  const tryHarderHints = new Map([
    [DecodeHintType.TRY_HARDER, true]
  ]);
  const pureHints = new Map([
    [DecodeHintType.TRY_HARDER, true],
    [DecodeHintType.PURE_BARCODE, true]
  ]);

  const results = [
    decodeVariant(
      'hybrid+tryHarder',
      packedRgb,
      width,
      height,
      tryHarderHints,
      source => new HybridBinarizer(source)
    ),
    decodeVariant(
      'hybrid+pure',
      packedRgb,
      width,
      height,
      pureHints,
      source => new HybridBinarizer(source)
    ),
    decodeVariant(
      'global+tryHarder',
      packedRgb,
      width,
      height,
      tryHarderHints,
      source => new GlobalHistogramBinarizer(source)
    ),
    decodeVariant(
      'global+pure',
      packedRgb,
      width,
      height,
      pureHints,
      source => new GlobalHistogramBinarizer(source)
    )
  ];

  if (!results.some(Boolean)) {
    process.exitCode = 1;
  }
}

run();
