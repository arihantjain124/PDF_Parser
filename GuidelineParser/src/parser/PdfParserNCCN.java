package parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

import parser.renderer.GuidelinePageRenderer;
import parser.text.GuidelineTextStripper;
import parser.text.WordWithBounds;

public final class PdfParserNCCN
{
    private static final Log LOG = LogFactory.getLog(ExtractText.class);

    private static final String PASSWORD = "-password";
    private static final String ENCODING = "-encoding";
    private static final String CONSOLE = "-console";
    private static final String START_PAGE = "-startPage";
    private static final String END_PAGE = "-endPage";
    private static final String SORT = "-sort";
    private static final String IGNORE_BEADS = "-ignoreBeads";
    private static final String DEBUG = "-debug";

    private static final String ALWAYSNEXT = "-alwaysNext";
    private static final String ROTATION_MAGIC = "-rotationMagic";
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
        boolean alwaysNext = false;
        boolean rotationMagic = false;
        
        String password = "";
        String encoding = STD_ENCODING;
        String pdfFile = null;
        String outputFile = null;
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
            else if (args[i].equals(ALWAYSNEXT))
            {
                alwaysNext = true;
            }
            else if (args[i].equals(ROTATION_MAGIC))
            {
                rotationMagic = true;
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

        if( pdfFile == null )
        {
            usage();
        }
        else
        {

            Writer output = null;
            PDDocument document = null;
            try
            {
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
                stripper = new GuidelineTextStripper(startPage);
                stripper.setSortByPosition(sort);
                stripper.setShouldSeparateByBeads(separateBeads);

                // Extract text for main document:
                extractPages(startPage, Math.min(endPage, document.getNumberOfPages()), 
                             stripper, document, output, rotationMagic, alwaysNext);
                
                if (true) {
                    List<WordWithBounds> wordRect = stripper.getWordBounds();
                    GuidelinePageRenderer renderer = new GuidelinePageRenderer(document,startPage,72);
                    renderer.intializeImage();
                    renderer.getGeometry();
                    renderer.drawLines();
                    renderer.drawTriangles();
                    renderer.drawWordBounds(wordRect);
                    renderer.OutputImage();
//                	ArrayList<GeneralPath> lines=renderer.getLines();
//                	ArrayList<GeneralPath> triangles=renderer.getTriangles();
                }
                
                

            }
            finally
            {
                IOUtils.closeQuietly(output);
                IOUtils.closeQuietly(document);
            }
        }
    }

    private void extractPages(int startPage, int endPage,
    		GuidelineTextStripper stripper, PDDocument document, Writer output,
            boolean rotationMagic, boolean alwaysNext) throws IOException
    {
        for (int p = startPage; p <= endPage; ++p)
        {
            stripper.setStartPage(p);
            stripper.setEndPage(p);
            try
            {
                if (rotationMagic)
                {
                    PDPage page = document.getPage(p - 1);
                    int rotation = page.getRotation();
                    page.setRotation(0);
                    AngleCollector angleCollector = new AngleCollector();
                    angleCollector.setStartPage(p);
                    angleCollector.setEndPage(p);
                    angleCollector.writeText(document, new NullWriter());
                    // rotation magic
                    for (int angle : angleCollector.getAngles())
                    {
                        // prepend a transformation
                        // (we could skip these parts for angle 0, but it doesn't matter much)
                        PDPageContentStream cs = new PDPageContentStream(document, page,
                                PDPageContentStream.AppendMode.PREPEND, false);
                        cs.transform(Matrix.getRotateInstance(-Math.toRadians(angle), 0, 0));
                        cs.close();

                        stripper.writeText(document, output);

                        // remove prepended transformation
                        ((COSArray) page.getCOSObject().getItem(COSName.CONTENTS)).remove(0);
                    }
                    page.setRotation(rotation);
                }
                else
                {
                    stripper.writeText(document, output);
                }
            }
            catch (IOException ex)
            {
                if (!alwaysNext)
                {
                    throw ex;
                }
                LOG.error("Failed to process page " + p, ex);
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
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar ExtractText [options] <inputfile> [output-text-file]\n"
            + "\nOptions:\n"
            + "  -password <password>        : Password to decrypt document\n"
            + "  -encoding <output encoding> : UTF-8 (default) or ISO-8859-1, UTF-16BE,\n"
            + "                                UTF-16LE, etc.\n"
            + "  -console                    : Send text to console instead of file\n"
            + "  -html                       : Output in HTML format instead of raw text\n"
            + "  -sort                       : Sort the text before writing\n"
            + "  -ignoreBeads                : Disables the separation by beads\n"
            + "  -debug                      : Enables debug output about the time consumption\n"
            + "                                of every stage\n"
            + "  -alwaysNext                 : Process next page (if applicable) despite\n"
            + "                                IOException (ignored when -html)\n"
            + "  -rotationMagic              : Analyze each page for rotated/skewed text,\n"
            + "                                rotate to 0° and extract separately\n"
            + "                                (slower, and ignored when -html)\n"
            + "  -startPage <number>         : The first page to start extraction (1 based)\n"
            + "  -endPage <number>           : The last page to extract (1 based, inclusive)\n"
            + "  <inputfile>                 : The PDF document to use\n"
            + "  [output-text-file]          : The file to write the text to";
        
        System.err.println(message);
        System.exit( 1 );
    }
}


