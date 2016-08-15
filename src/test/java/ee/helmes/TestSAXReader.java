package ee.helmes;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogManager;

import static org.junit.Assert.*;


public class TestSAXReader {

    private SAXReader saxReader;
    private LinkedBlockingQueue<Attributes> attributes;


    @Before
    public void setUp() throws IOException {
        String XMLFileName = "/rules.xml";
        File resourceXMLFile = new File(SAXReader.class.getResource(XMLFileName).getFile());
        attributes = new LinkedBlockingQueue<>();
        saxReader = new SAXReader(resourceXMLFile,attributes);


    }

    @Test
    public void testGetAttributesList() throws Exception {

        assertEquals(attributes,saxReader.getAttributesList());
    }

    @Test
    public void testIsXmlFileIsParsed() throws Exception {
        assertFalse(saxReader.isErrorWhileParse());
        Field errorWhileParse = SAXReader.class.getDeclaredField("errorWhileParse");
        errorWhileParse.setAccessible(true);
        errorWhileParse.set(saxReader,true);
        assertTrue(saxReader.isErrorWhileParse());

    }

    @Test
    public void testRun() throws Exception {

        Field resXMLFile = SAXReader.class.getDeclaredField("xmlFile");
        resXMLFile.setAccessible(true);
        resXMLFile.set(saxReader, null);
        Thread parsingThread = new Thread(saxReader);
        parsingThread.start();
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException ex){
            fail();
        }
        assertFalse(parsingThread.isAlive());
        assertTrue(saxReader.isErrorWhileParse());


    }


}