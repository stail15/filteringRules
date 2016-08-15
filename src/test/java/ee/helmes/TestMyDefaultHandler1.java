package ee.helmes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.Attributes;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogManager;

import static org.junit.Assert.*;



@RunWith(Parameterized.class)
public class TestMyDefaultHandler1 {

    private SAXReader saxReader;
    private File sourceFile;
    private String nodePath;
    private boolean expect;


    public TestMyDefaultHandler1(String nodePath, boolean expect){
        this.nodePath = nodePath;
        this.expect = expect;
    }

    @Before
    public void setUp(){


        try {
            sourceFile = File.createTempFile("temp", ".xml");
            sourceFile.deleteOnExit();
        }
        catch (IOException ex){
            fail();
        }

        saxReader = new SAXReader(sourceFile,new LinkedBlockingQueue<Attributes>());

    }


    @Test
    public void testValidateNodePath() throws Exception {
       boolean result = saxReader.getDefaultHandler().validateNodePath(nodePath);
        assertEquals(expect,result);
    }

    @Parameterized.Parameters
    public static Collection validArgs() {
        return Arrays.asList(new Object[][]{
                {"/rules",false},
                {"/rules/rule",true},
                {"rules/rule",true},
                {"rule/rules",false},
                {"root/rule",false},
                {"/root/rules/rule",true},
                {"/",false},
                {"rules/rule",true},
                {null,false}
        });
    }
}