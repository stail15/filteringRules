package ee.helmes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogManager;

import static org.junit.Assert.*;


public class TestMain1 {


    private SAXReader saxReader;
    private XMLNodeFilter xmlNodeFilter;
    private File resourceXMLFile;
    private File resultXMLFile;



    @Before
    public void setUp() throws IOException{

        String resourceFileName = "/rules.xml";
        resourceXMLFile = new File(Main.class.getResource(resourceFileName).getFile());

        String resultFileName = "/rulesResult.xml";
        resultXMLFile = new File(Main.class.getResource(resultFileName).getFile());



        LinkedBlockingQueue<Attributes> attributes = new LinkedBlockingQueue<>();
        saxReader = new SAXReader(resourceXMLFile, attributes);
        xmlNodeFilter = new XMLNodeFilter(resultXMLFile,saxReader);
    }

    @Test
    public void testConfigLogger(){

       assertTrue(Main.configLogger("/logging.properties"));
    }


    @Test
    public void testGetSourceFile1() throws Exception {
        File expectFile = resourceXMLFile;
        String sourceFileName = expectFile.getAbsolutePath();
        File sourceFile = Main.getSourceFile(sourceFileName);

        assertEquals(sourceFile, expectFile);
    }

    @Test (expected = FileNotFoundException.class)
    public void testGetSourceFile2() throws Exception {
        String fileName = "testFileToThrowException";
        Main.getSourceFile(fileName);
    }

    @Test
    public void testGetResultFile1() throws Exception{

        File expectedFile = resultXMLFile;
        String resultFileName = resultXMLFile.getAbsolutePath();
        File resultFile = Main.getResultFile(resultFileName);

        assertEquals(expectedFile, resultFile);
    }

    @Test
    public void testCreateFilteringThread() throws Exception {
        Thread filteringThread = Main.createFilteringThread(xmlNodeFilter);
        Field target = Thread.class.getDeclaredField("target");

        target.setAccessible(true);
        Object runnable = target.get(filteringThread);

        assertEquals(xmlNodeFilter,runnable);
    }

    @Test
    public void testCreateParsingThread() throws Exception{
        Thread parsingThread = Main.createParsingThread(saxReader);
        Field target = Thread.class.getDeclaredField("target");

        target.setAccessible(true);
        Object runnable = target.get(parsingThread);

        assertEquals(saxReader,runnable);

    }


}
