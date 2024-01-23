package net.oneoverzero.common.itext;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class contains the logic used to remove all Javascript from a PDF.
 *
 * @author lombardo
 */
public class PdfJavascriptStripper {

    private static final PdfString emptyPdfString = new PdfString("");
    private static boolean containsJavaScript=false;

    /**
     * Strips the given PDF file of all Javascript parts and writes the result in a new file.
     * 
     * @param args the first element is the file to read, the second is the file to write.
     *
     */
    public static void main(final String[] args) throws IOException, DocumentException {
        // check arguments
        if (args.length != 2) {
            System.out.println("Wrong number of arguments.");
            System.out.println("PdfJavascriptStripper in.pdf out.pdf");
            System.exit(0);
        }

        System.out.println("Reading: " + args[0]);
        
        FileInputStream fis = new FileInputStream(args[0]);
        FileOutputStream fos = new FileOutputStream(args[1]);

        boolean foundJS = stripJavascript(fis, fos);
        fis.close();
        
        if (foundJS) {
            System.out.println("The input file contained Javascript! Created a sanitized outputfile.");
        }
        
        fos.close();
    }

    /**
     * Strips all Javascript from the given PDF.
     *
     * The logic is the following:
     *
     * We start traversing the PDF structure by some well known entry points, then we traverse the PAGE structure
     * recursively.
     *
     * We replace all the Javascript code encountered with an empty String.
     * 
     * @param pdfInputStream of the PDF
     * @return a Pair containing the the stripped PDF and a boolean flag that tells whether we encountered and stripped
     *         some JS parts.
     */
    public static boolean stripJavascript(InputStream pdfInputStream, OutputStream javaScriptFreeResult) throws IOException, DocumentException {
        // Since the PDF document can contain cross-linked structures, we need
        // to keep track of the elements already traversed, to avoid infinite
        // loops.
        final Set<PdfObject> traversed = new HashSet<>();

        final PdfReader reader = new PdfReader(pdfInputStream);
        PdfStamper stamper = new PdfStamper(reader, javaScriptFreeResult);

        // start at the trailer
        final PdfDictionary trailerDictionary = reader.getTrailer();
        traversePdfDictionary(trailerDictionary, traversed);

        // Iterate on the indirect references by number - probably redundant
        int numObjs = reader.getXrefSize();
        for (int i = 0; i < numObjs; ++i) {
            final PdfObject curObj = reader.getPdfObject(i); // no need to worry about indirect references this way

            if (!traversed.contains(curObj)) {
                if (curObj instanceof PdfDictionary) {
                    traversePdfDictionary((PdfDictionary) curObj, traversed);
                } else if (curObj instanceof PdfArray) {
                    traversePdfArray((PdfArray) curObj, traversed);
                }
            }
        }

        // Remove document level Javascripts
        try {
            reader.getCatalog().getAsDict(PdfName.NAMES).remove(PdfName.JAVASCRIPT);
        } catch (Exception eee) {

        }
        // This will remove orhpaned objects - possibly reclaiming space
        reader.removeUnusedObjects();

        stamper.close();
        return containsJavaScript;
    }

    /**
     * Traverses recursively the given PdfDictionary
     * 
     * @param dict Can be a PdfDictionary
     * @param traversed the list of PDF elements already traversed.
     * 
     */
    public static void traversePdfDictionary(final PdfDictionary dict, final Set<PdfObject> traversed) {
        if (traversed.contains(dict)) {
            return;
        } else {
            traversed.add(dict);
        }

        for (PdfName key : dict.getKeys()) {
            PdfObject data = dict.getDirectObject(key);

            if (key.toString().trim().equalsIgnoreCase("/JS")) {
                dict.put(key, emptyPdfString);
                containsJavaScript=true;
            }
            else if (!key.toString().trim().equalsIgnoreCase("/Parent") && data instanceof PdfDictionary) {
                traversePdfDictionary((PdfDictionary) data, traversed);
            } else if (!key.toString().trim().equalsIgnoreCase("/Parent") && data instanceof PdfArray) {
                traversePdfArray((PdfArray) data, traversed);
            }
        }
    }

    /**
     * Traverses recursively the given PdfArray
     * 
     * @param array a PdfArray
     * @param traversed the list of PDF elements already traversed.
     */
    public static void traversePdfArray(final PdfArray array, final Set<PdfObject> traversed) {
        if (traversed.contains(array)) {
            return;
        } else {
            traversed.add(array);
        }
        final Iterator<?> iter = array.listIterator();
        while (iter.hasNext()) {
            Object data = iter.next();
            if (data instanceof PRIndirectReference) {
                data = PdfReader.getPdfObject((PRIndirectReference)data);
            }

            if (data instanceof PdfDictionary) {
                traversePdfDictionary((PdfDictionary) data, traversed);
            } else if (data instanceof PdfArray) {
                traversePdfArray((PdfArray) data, traversed);
            }      
        }
    }
}