# PDF Guideline Parser

**A Java pipeline for converting structured oncology guideline PDFs into machine-readable JSON-LD.**

This project extracts more than plain text. It is tailored to NCCN-style clinical guideline documents, where pages combine flowcharts, arrows, labels, tables, footnotes, bookmarks, update pages, and narrative text. It analyses PDF layout and drawing geometry to reconstruct those elements into a structured representation that downstream applications can inspect or render.

> This is a research/prototype parser with document-specific configuration. It is not a general-purpose PDF parser and should not be used for clinical decision-making without independent validation.

## What it does

- Extracts headings, text regions, labels, footnotes, bookmarks, and update-page content.
- Detects flowchart lines, triangles, fan-out paths, and region-to-region relationships.
- Extracts configured tables with Tabula and can convert selected table rows into flow nodes.
- Produces JSON-LD containing graph objects, text, labels, tables, footnotes, updates, and bookmarks.
- Optionally renders extracted graph objects to images for visual verification.

```text
Guideline PDF
    │
    ├── Text + footnotes + bookmarks
    ├── Page geometry → lines, triangles, regions, graph edges
    └── Configured tables → structured table data / flow nodes
                                  │
                                  ▼
                         JSON-LD + optional images
```

## Repository layout

| Path | Purpose |
| --- | --- |
| `GuidelineParser/src/parser/` | Main Java parser and command-line entry point. |
| `GuidelineParser/src/parser/page/` | Page traversal, bookmark extraction, and orchestration. |
| `GuidelineParser/src/parser/graphics/` | Flowchart line, arrow/triangle, and fan-out graph analysis. |
| `GuidelineParser/src/parser/text/` | Text stripping, region analysis, labels, and footnotes. |
| `GuidelineParser/src/parser/table/` | Tabula-backed table extraction. |
| `GuidelineParser/src/parser/json/` | JSON-LD model objects and serializers. |
| `GuidelineParser/*.properties` | Per-guideline page regions, table areas, regexes, and output identity. |
| `GuidelineParser/lib/` | Checked-in PDFBox, Tabula, Gson, and JavaTuples dependencies. |
| `imagingTrackPython/` | Standalone OpenCV/Tesseract experiments for ROI OCR, Hough lines, and template matching. |
| `pdfbox/` | Vendored Apache PDFBox source and examples; it is a third-party dependency, not the parser implementation. |

## Prerequisites

- JDK 8 or later.
- An NCCN-style PDF that you are licensed and authorised to process.
- The checked-in JAR files in `GuidelineParser/lib/`.

The repository is an Eclipse project and does not include a Maven or Gradle build for the `GuidelineParser` module. The commands below compile it directly with the included JARs.

## Build

```bash
cd GuidelineParser
mkdir -p bin
javac -cp "lib/*" -d bin $(find src -name "*.java")
```

Alternatively, import `GuidelineParser/` into Eclipse as an existing project. Its `.classpath` file references the same JARs under `lib/`.

## Run the parser

`parser.PdfParserNCCN` is the primary entry point. It requires a configuration file, a guideline version, and an input PDF.

```bash
cd GuidelineParser

java -cp "bin:lib/*" parser.PdfParserNCCN \
  -config NCCN_config.properties \
  -version 1.0 \
  -startPage 1 \
  -endPage 10 \
  -generateImage \
  /path/to/guideline.pdf parsed-output.txt
```

The parser creates JSON-LD under `GuidelineParser/jsonexport/` using the configured guideline name, version, and selected page range. `parsed-output.txt` receives the extracted text stream. Omit `-generateImage` to skip visual graph-verification images.

### Command-line options

| Option | Description |
| --- | --- |
| `-config <file>` | Required properties file that defines page regions, table areas, regexes, and JSON-LD identity. |
| `-version <value>` | Guideline version embedded in the generated filename. |
| `-startPage <n>` / `-endPage <n>` | Inclusive page range; defaults to the whole document. |
| `-generateImage` | Renders extracted graph JSON objects for inspection. |
| `-password <password>` | Password for an encrypted PDF. |
| `-sort` | Sorts text by position before extraction. |
| `-console` | Writes the text stream to standard output. |
| `-debug` | Emits processing timing and output information. |

## Configuration

The supplied `NCCN_config.properties` is an example for a particular NCCN NSCLC guideline layout. It contains document-specific information such as:

- bounding boxes for the main, heading, update, and page-key regions;
- regular expressions for page keys, staging values, and footnote references;
- table page numbers, extraction rectangles, and extraction modes; and
- the JSON-LD identifier and output guideline name.

Create and tune a separate properties file for each guideline family and version. A change in page layout, table position, font usage, or flowchart styling can require new coordinates or parser rules.

## Python image experiments

`imagingTrackPython/` is independent of the Java parser. Its scripts demonstrate:

- interactive OCR over a selected image region with OpenCV and Tesseract;
- line detection with a Hough transform; and
- template matching against sample images.

They use local sample image paths and, in the OCR script, a machine-specific Tesseract path. Update those paths before running the experiments.

## Limitations

- The implementation is specialised to structured oncology guideline PDFs and configured layouts.
- No source PDFs or generated JSON outputs are bundled with the repository.
- Extraction quality depends on the PDF's vector geometry and its match to the configuration; scanned PDFs need an OCR workflow.
- This repository does not declare a project-wide license. The vendored PDFBox subtree retains its own Apache-2.0 licensing materials.

## Project history

Contribution activity is available in the repository's [GitHub contributors graph](https://github.com/arihantjain124/PDF_Parser/graphs/contributors?from=04%2F07%2F2026).
