package parser.table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;

import parser.config.ConfigProperty;
import technology.tabula.detectors.DetectionAlgorithm;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;
import technology.tabula.writers.JSONWriter;
import technology.tabula.writers.TSVWriter;
import technology.tabula.writers.Writer;
import technology.tabula.Rectangle;
import technology.tabula.Ruling;
import technology.tabula.Table;
import technology.tabula.Utils;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Pair;


public class GuidelineTableExtractor {

    private static final int RELATIVE_AREA_CALCULATION_MODE = 0;
    private static final int ABSOLUTE_AREA_CALCULATION_MODE = 1;


    private List<Integer> pages;
    private OutputFormat outputFormat;
    private String password;
    private TableExtractor tableExtractor;

    public GuidelineTableExtractor(String password) throws ParseException, IOException {
        this.pages = GuidelineTableExtractor.whichPages();
        this.outputFormat = OutputFormat.JSON;
        this.tableExtractor = GuidelineTableExtractor.createExtractor();

        this.password = password;
    }

    public void extractTables(File pdfFile, File outputFile) throws ParseException {
        if (!pdfFile.exists()) {
            throw new ParseException("File does not exist");
        }
        extractFileTables(pdfFile, outputFile);
    }
    
    public void extractFileTables(File pdfFile, File outputFile) throws ParseException {
        extractFileInto(pdfFile, outputFile);
    }

    public void extractFileInto(File pdfFile, File outputFile) throws ParseException {
        BufferedWriter bufferedWriter = null;
        try {
            FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);

            outputFile.createNewFile();
            extractFile(pdfFile, bufferedWriter);
        } catch (IOException e) {
            throw new ParseException("Cannot create file " + outputFile);
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    System.out.println("Error in closing the BufferedWriter" + e);
                }
            }
        }
    }

    private void extractFile(File pdfFile, Appendable outFile) throws ParseException {
        PDDocument pdfDocument = null;
        try {
            pdfDocument = this.password == null ? PDDocument.load(pdfFile) : PDDocument.load(pdfFile, this.password);
            PageIterator pageIterator = getPageIterator(pdfDocument);
            List<Table> tables = new ArrayList<>();

            while (pageIterator.hasNext()) {
                Page page = pageIterator.next();

                if (tableExtractor.verticalRulingPositions != null) {
                    for (Float verticalRulingPosition : tableExtractor.verticalRulingPositions) {
                        page.addRuling(new Ruling(0, verticalRulingPosition, 0.0f, (float) page.getHeight()));
                    }
                }
                
                int pageNumber = page.getPageNumber();
                String areaStrList = ConfigProperty.getProperty(pageNumber + ".table.rect");
                List<Pair<Integer, Rectangle>> areaList = null;
                
                if(areaStrList != null) {
                	areaList = new ArrayList<Pair<Integer, Rectangle>>();
                	String[] areaStrs = areaStrList.split(";");
                	
                	for(String areaStr : areaStrs) {
		                List<Float> f = parseFloatList(areaStr);
		                if (f.size() != 4) {
		                    throw new ParseException("area parameters must be top,left,bottom,right optionally preceded by %");
		                }
		                areaList.add(new Pair<Integer, Rectangle>(ABSOLUTE_AREA_CALCULATION_MODE, new Rectangle(f.get(0), f.get(1), f.get(3) - f.get(1), f.get(2) - f.get(0))));
                	}
                }
                
                String extractionMethod = ConfigProperty.getProperty(pageNumber + ".table.mode");
                if(extractionMethod != null) {
                	
                	if (extractionMethod.equalsIgnoreCase("l")) {
                		tableExtractor.setMethod(ExtractionMethod.SPREADSHEET);
                    }

                    // -n/--no-spreadsheet [deprecated; use -t] or  -c/--columns or -g/--guess or -t/--stream
                    if (extractionMethod.equalsIgnoreCase("t")) {
                    	tableExtractor.setMethod(ExtractionMethod.BASIC);
                    }
                	
                }else {
                	tableExtractor.setMethod(ExtractionMethod.DECIDE);
                }

                if (areaList != null) {
                    for (Pair<Integer, Rectangle> areaPair : areaList) {
                        Rectangle area = areaPair.getRight();
                        if (areaPair.getLeft() == RELATIVE_AREA_CALCULATION_MODE) {
                            area = new Rectangle((float) (area.getTop() / 100 * page.getHeight()),
                                    (float) (area.getLeft() / 100 * page.getWidth()), (float) (area.getWidth() / 100 * page.getWidth()),
                                    (float) (area.getHeight() / 100 * page.getHeight()));
                        }
                        tables.addAll(tableExtractor.extractTables(page.getArea(area)));
                    }
                } else {
                    tables.addAll(tableExtractor.extractTables(page));
                }
            }
            writeTables(tables, outFile);
        } catch (IOException e) {
            throw new ParseException(e.getMessage());
        } finally {
            try {
                if (pdfDocument != null) {
                    pdfDocument.close();
                }
            } catch (IOException e) {
                System.out.println("Error in closing pdf document" + e);
            }
        }
    }

    private PageIterator getPageIterator(PDDocument pdfDocument) throws IOException {
        ObjectExtractor extractor = new ObjectExtractor(pdfDocument);
        return (pages == null) ?
                extractor.extract() :
                extractor.extract(pages);
    }

    // CommandLine parsing methods

    private static List<Integer> whichPages() throws ParseException, IOException {
    	String pagesOption = ConfigProperty.getProperty("page.tables");
        return Utils.parsePagesOption(pagesOption);
    }

    private static TableExtractor createExtractor() throws ParseException {
        TableExtractor extractor = new TableExtractor();
        extractor.setGuess(false);
        extractor.setMethod(ExtractionMethod.DECIDE);
        extractor.setUseLineReturns(false);

        return extractor;
    }

    // utilities, etc.

    public static List<Float> parseFloatList(String option) throws ParseException {
        String[] f = option.split(",");
        List<Float> rv = new ArrayList<>();
        try {
            for (final String element : f) {
                rv.add(Float.parseFloat(element));
            }
            return rv;
        } catch (NumberFormatException e) {
            throw new ParseException("Wrong number syntax");
        }
    }

    private static class TableExtractor {
        private boolean guess = false;
        private boolean useLineReturns = false;
        private BasicExtractionAlgorithm basicExtractor = new BasicExtractionAlgorithm();
        private SpreadsheetExtractionAlgorithm spreadsheetExtractor = new SpreadsheetExtractionAlgorithm();

        private boolean verticalRulingPositionsRelative = false;
        private List<Float> verticalRulingPositions = null;

        private ExtractionMethod method = ExtractionMethod.BASIC;

        public TableExtractor() {
        }

        public void setGuess(boolean guess) {
            this.guess = guess;
        }

        public void setUseLineReturns(boolean useLineReturns) {
            this.useLineReturns = useLineReturns;
        }

        public void setMethod(ExtractionMethod method) {
            this.method = method;
        }

        public List<Table> extractTables(Page page) {
            ExtractionMethod effectiveMethod = this.method;
            if (effectiveMethod == ExtractionMethod.DECIDE) {
                effectiveMethod = spreadsheetExtractor.isTabular(page) ?
                        ExtractionMethod.SPREADSHEET :
                        ExtractionMethod.BASIC;
            }
            switch (effectiveMethod) {
                case BASIC:
                    return extractTablesBasic(page);
                case SPREADSHEET:
                    return extractTablesSpreadsheet(page);
                default:
                    return new ArrayList<>();
            }
        }

        public List<Table> extractTablesBasic(Page page) {
            if (guess) {
                // guess the page areas to extract using a detection algorithm
                // currently we only have a detector that uses spreadsheets to find table areas
                DetectionAlgorithm detector = new NurminenDetectionAlgorithm();
                List<Rectangle> guesses = detector.detect(page);
                List<Table> tables = new ArrayList<>();

                for (Rectangle guessRect : guesses) {
                    Page guess = page.getArea(guessRect);
                    tables.addAll(basicExtractor.extract(guess));
                }
                return tables;
            }

            if (verticalRulingPositions != null) {
                List<Float> absoluteRulingPositions;

                if (this.verticalRulingPositionsRelative) {
                    // convert relative to absolute
                    absoluteRulingPositions = new ArrayList<>(verticalRulingPositions.size());
                    for (float relative : this.verticalRulingPositions) {
                        float absolute = (float) (relative / 100.0 * page.getWidth());
                        absoluteRulingPositions.add(absolute);
                    }
                } else {
                    absoluteRulingPositions = this.verticalRulingPositions;
                }
                return basicExtractor.extract(page, absoluteRulingPositions);
            }

            return basicExtractor.extract(page);
        }

        public List<Table> extractTablesSpreadsheet(Page page) {
            // TODO add useLineReturns
            return spreadsheetExtractor.extract(page);
        }
    }

    private void writeTables(List<Table> tables, Appendable out) throws IOException {
        Writer writer = null;
        switch (outputFormat) {
            case CSV:
                writer = new CSVWriter();
                break;
            case JSON:
                writer = new JSONWriter();
                break;
            case TSV:
                writer = new TSVWriter();
                break;
        }
        writer.write(out, tables);
    }

    private enum OutputFormat {
        CSV,
        TSV,
        JSON;
    }

    private enum ExtractionMethod {
        BASIC,
        SPREADSHEET,
        DECIDE
    }
}
