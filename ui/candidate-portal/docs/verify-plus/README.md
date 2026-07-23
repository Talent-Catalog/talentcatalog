# Verify+ Mock QR Test Assets

This folder is reserved for mock, non-production QR images used to manually test the candidate portal Verify+ flow.

No official UNHCR sample QR is available yet, so these fixtures are intentionally synthetic.

## Payload Contract

The current backend mock parser accepts JSON payloads with this structure:

```json
{"v":"mock-1","unhcrId":"..."}
```

Rules:
- `v` must be exactly `mock-1`
- `unhcrId` must be present for valid payloads
- The encoded payload should stay byte-exact (avoid tools that escape or reformat the string)

## Sample Files

This folder contains the following QR encoded payloads:

- `verify-plus-valid.png` -> `{"v":"mock-1","unhcrId":"123-45C67890"}`
- `verify-plus-valid-duplicate.png` -> `{"v":"mock-1","unhcrId":"999-00A11111"}`
- `verify-plus-invalid-version.png` -> `{"v":"mock-2","unhcrId":"123-45C67890"}`
- `verify-plus-missing-unhcr-id.png` -> `{"v":"mock-1"}`
- `verify-plus-malformed-json.png` -> `{"v":"mock-1","unhcrId":"123-45C67890"`
- `verify-plus-non-json.png` -> `hello world`

## Generation Commands

Using `qrencode`:

```bash
qrencode -o verify-plus-valid.png '{"v":"mock-1","unhcrId":"123-45C67890"}'
```

Using Node:

```bash
npx qrcode '{"v":"mock-1","unhcrId":"123-45C67890"}' -o verify-plus-valid.png
```

Each sample is encoded with the corresponding payload text above, the resulting PNG files reside in this directory.

## Notes

- These files are documentation/test fixtures only.
- They are not referenced by application code or automated tests.
- PNG assets are generated manually in support of iterative development and testing.
