package ee.helmes;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.Assert.*;


public class TestResultTransformer {

    private ResTransformer resTransformer;
    private File result;
    private File expected;
    private Document resultDocument;
    private DOMSource domSource;

    @Before
    public void setUp(){

        expected = new File(this.getClass().getResource("/rulesResult.xml").getFile());
        resultDocument = this.getResultDocument();
        domSource = new DOMSource(resultDocument);
        resTransformer = new ResTransformer(resultDocument);
        try {

            result = File.createTempFile("result",".xml");
            result.deleteOnExit();
        }
        catch (IOException ex){
            fail();
        }

    }

    @Test
    public void testSaveResultToFile() throws Exception {

        boolean saved = resTransformer.saveResultToFile(result, domSource, TransformerFactory.newInstance());

        assertTrue(saved);

        assertTrue(compareFiles(expected,result));


    }

    @Test
    public void testProduceHTML() throws Exception {

        Object result = resTransformer.produceHTML(domSource, TransformerFactory.newInstance());

        assertNotNull(result);
        assertTrue(result instanceof File);


    }


    private Document getResultDocument(){
        File sourceFile = new File(this.getClass().getResource("/rulesResult.xml").getFile());
        Document resultDocument = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder =factory.newDocumentBuilder();
            resultDocument = builder.parse(sourceFile);

        }
        catch (ParserConfigurationException | SAXException | IOException ex){
            ex.printStackTrace();
            fail();
        }
        return resultDocument;
    }


    private static boolean compareFiles(File file1, File file2){
        boolean equal = false;

        if(file1==null || file2 == null){
            return equal;
        }
        if(!(file1.length()==file2.length())){
            return equal;
        }

        try {
            Scanner scanner1 = new Scanner(file1);
            Scanner scanner2 = new Scanner(file2);
            while (scanner1.hasNext()|| scanner2.hasNext()){
                String scan1 = scanner1.next();
                String scan2 = scanner2.next();
                if(!(scan1 .equals(scan2) )){
                    return equal;
                }
            }
            equal = true;
            scanner1.close();
            scanner2.close();
        }
        catch (FileNotFoundException ex){
            fail();
        }


        return equal;
    }
}