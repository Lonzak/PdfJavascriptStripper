package net.oneoverzero.common.itext;

import com.lowagie.text.DocumentException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author lonzak
 */
public class PdfJavascriptStripperTest {
    
    public static final String MAVEN_DEFAULT_OUTPUT_DIR = "./target";
    public static final String MAVEN_DEFAULT_RESOURCE_DIR = "./src/test/resources";
    
    @Test
    public void testJavaScriptRemoval() throws DocumentException, IOException {
        InputStream fis = loadFileFromClasspath(this.getClass(),"input.pdf");
        FileOutputStream fos = createFileStreamInMavenOutputDir("output.pdf");
        
        boolean containedJS = PdfJavascriptStripper.stripJavascript(fis,fos);

        Assert.assertTrue(containedJS);
        
        fos.close();
    }
    
    @Test
    public void testJavaScriptRemoval2nd() throws DocumentException, IOException {
        InputStream fis = loadFileFromClasspath(this.getClass(),"calc.pdf");
        FileOutputStream fos = createFileStreamInMavenOutputDir("calc-out.pdf");
        
        boolean containedJS = PdfJavascriptStripper.stripJavascript(fis,fos);

        Assert.assertTrue(containedJS);
        
        fos.close();
    }
    
    @Ignore
    public void testJavaScriptRemovalMultipleFiles() throws DocumentException, IOException {
        
        File folder = new File(MAVEN_DEFAULT_RESOURCE_DIR);
        File[] files = folder.listFiles(new PdfFilenameFilter());
        
        for(File file : files) {
            
            System.out.println("Processing "+file.getName());
            
            InputStream fis = loadFileFromClasspath(this.getClass(),file.getName());
            FileOutputStream fos = createFileStreamInMavenOutputDir("output-"+file.getName());
            
            boolean containedJS = PdfJavascriptStripper.stripJavascript(fis,fos);
    
            //Assert.assertTrue(containedJS);
            
            fos.close();
        }
    }
    
    /**
     * Loads binary file from class path.
     * 
     * @param clazz unit test class to fetch the class loader from
     * @param classpath path to the file with '/' as folder delimiter (from root '/myfolder/myfile.xml')
     * @return test resource as stream
     */
    public static InputStream loadFileFromClasspath(Class<?> clazz, String classpath) {     

        //Class<?> testClass must be used, not raw Class -> getResourceAsStream() will not work otherwise! 
        
        InputStream is = clazz.getResourceAsStream(classpath);
        
        if(is==null) {
            is = clazz.getResourceAsStream("/"+classpath);
            
            if(is==null) {
                try {
                    is = new FileInputStream(classpath);
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException("Resource '" + classpath + "' not found.");
                }
            }
        }
        
        return is;
    }
    
    /**
     * Create a file in the default maven output directory (target)
     * 
     * @param filename to write
     * @return FileOutputStream of created file
     */
    public static FileOutputStream createFileStreamInMavenOutputDir(String filename) {      

        try {
            if(filename.startsWith("/")){
                return new FileOutputStream(MAVEN_DEFAULT_OUTPUT_DIR+filename);
            }
            else{
                return new FileOutputStream(MAVEN_DEFAULT_OUTPUT_DIR+"/"+filename);
            }
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("Can't create file '" + MAVEN_DEFAULT_OUTPUT_DIR+filename+ "'.", e);
        }
    }
    
    public class PdfFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".pdf");
        }
    };
}
