package parser;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

import parser.config.ConfigProperty;
import parser.page.PageProcessor;
import parser.text.GuidelineTextStripper;

public final class PdfParserNCCN
{
    private static final String PASSWORD = "-password";
    private static final String CONFIG_FILE = "-config";
    private static final String VERSION = "-version";
    private static final String ENCODING = "-encoding";
    private static final String CONSOLE = "-console";
    private static final String START_PAGE = "-startPage";
    private static final String END_PAGE = "-endPage";
    private static final String SORT = "-sort";
    private static final String IGNORE_BEADS = "-ignoreBeads";
    private static final String GENERATE_IMAGE = "-generateImage";
    private static final String DEBUG = "-debug";

    private static final String STD_ENCODING = "UTF-8";

    /*
     * debug flag
     */
    private boolean debugOutput = false;

    /**
     * private constructor.
    */
    private PdfParserNCCN()
    {
        //static class
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws IOException if there is an error reading the document or extracting the text.
     */
    public static void main( String[] args ) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        PdfParserNCCN extractor = new PdfParserNCCN();
        extractor.startExtraction(args);
    }
    /**
     * Starts the text extraction.
     *  
     * @param args the commandline arguments.
     * @throws IOException if there is an error reading the document or extracting the text.
     */
    public void startExtraction( String[] args ) throws IOException
    {
        boolean toConsole = false;
        boolean sort = false;
        boolean separateBeads = true;
        boolean generateImage = false;
        
        String password = "";
        String config = null;
        String encoding = STD_ENCODING;
        String pdfFile = null;
        String outputFile = null;
        String version = "1"; //Default value
        // Defaults to text files
        String ext = ".txt";
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;
        for( int i=0; i<args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            else if( args[i].equals( VERSION ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                version = args[i];
            }
            else if( args[i].equals( CONFIG_FILE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                config = args[i];
            }
            else if( args[i].equals( ENCODING ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                encoding = args[i];
            }
            else if( args[i].equals( START_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                startPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( SORT ) )
            {
                sort = true;
            }
            else if( args[i].equals( IGNORE_BEADS ) )
            {
                separateBeads = false;
            }
            else if( args[i].equals( GENERATE_IMAGE ) )
            {
                generateImage = true;
            }
            else if( args[i].equals( DEBUG ) )
            {
                debugOutput = true;
            }
            else if( args[i].equals( END_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                endPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( CONSOLE ) )
            {
                toConsole = true;
            }
            else
            {
                if( pdfFile == null )
                {
                    pdfFile = args[i];
                }
                else
                {
                    outputFile = args[i];
                }
            }
        }

        if( pdfFile == null || config == null )
        {
            usage();
        }
        else
        {

            Writer output = null;
            PDDocument document = null;
            try
            {
				ConfigProperty.setConfigFilePath(config);
				ConfigProperty.setVersion(version);
            	String[] regionOfInterest = ConfigProperty.getProperty("page.main-content.region").split("[,]");
                Rectangle mainContentRect = new Rectangle(Integer.valueOf(regionOfInterest[0]),Integer.valueOf(regionOfInterest[1]),Integer.valueOf(regionOfInterest[2]),Integer.valueOf(regionOfInterest[3]));

                regionOfInterest = ConfigProperty.getProperty("page.update-content.region").split("[,]");
                Rectangle totalRegion = new Rectangle(Integer.valueOf(regionOfInterest[0]),Integer.valueOf(regionOfInterest[1]),Integer.valueOf(regionOfInterest[2]),Integer.valueOf(regionOfInterest[3]));
                
                long startTime = startProcessing("Loading PDF "+pdfFile);
                if( outputFile == null && pdfFile.length() >4 )
                {
                    outputFile = new File( pdfFile.substring( 0, pdfFile.length() -4 ) + ext ).getAbsolutePath();
                }
                document = PDDocument.load(new File( pdfFile ), password);
                //Loading the pdf file using PDDocument
                AccessPermission ap = document.getCurrentAccessPermission();
                if( ! ap.canExtractContent() )
                {
                    // throw new IOException( "You do not have permission to extract text" );
                }
                
                stopProcessing("Time for loading: ", startTime);

                if( toConsole )
                {
                    output = new OutputStreamWriter( System.out, encoding );
                }
                else
                {
                    output = new OutputStreamWriter( new FileOutputStream( outputFile ), encoding );
                }
                startTime = startProcessing("Starting text extraction");
                if (debugOutput)
                {
                    System.err.println("Writing to " + outputFile);
                }
                GuidelineTextStripper stripper;
                GuidelineTextStripper uptStripper;

				uptStripper = new GuidelineTextStripper(startPage);
				uptStripper.setSortByPosition(sort);
				uptStripper.setShouldSeparateByBeads(separateBeads);
				uptStripper.addRegion( "MainContent", totalRegion );
				
				stripper = new GuidelineTextStripper(startPage);
				stripper.setSortByPosition(sort);
				stripper.setShouldSeparateByBeads(separateBeads);
				stripper.addRegion( "MainContent", mainContentRect );
				PageProcessor pageProcessor = new PageProcessor();
				pageProcessor.processPages(startPage, Math.min(endPage, document.getNumberOfPages()), generateImage, stripper,
						uptStripper, document, output);
                //Use the following code to generate JSON of individual pages when input is page range.
                //for (int p = startPage; p <= endPage; ++p)
                //{ 
                //	pageProcessor.processPages(p, Math.min(p, document.getNumberOfPages()), generateImage, stripper, uptStripper, document, output);
                //}
            } 
            finally
            {
                IOUtils.closeQuietly(output);
                IOUtils.closeQuietly(document);
            }
        }
    }

    private long startProcessing(String message) 
    {
        if (debugOutput) 
        {
            System.err.println(message);
        }
        return System.currentTimeMillis();
    }
    
    private void stopProcessing(String message, long startTime) 
    {
        if (debugOutput)
        {
            long stopTime = System.currentTimeMillis();
            float elapsedTime = ((float)(stopTime - startTime))/1000;
            System.err.println(message + elapsedTime + " seconds");
        }
    }

    static int getAngle(TextPosition text)
    {
        Matrix m = text.getTextMatrix().clone();
        m.concatenate(text.getFont().getFontMatrix());
        return (int) Math.round(Math.toDegrees(Math.atan2(m.getShearY(), m.getScaleY())));
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        String message = "java -jar guideline-parser.jar -startPage <start page no.> -endPage <end page no.> -version <guideline version> -generateImage(optional)"
        		+ "-config <config file path> <guideline pdf file path>";
        
        System.err.println(message);
        System.exit( 1 );
    }
}


