package ee.helmes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogManager;


@RunWith(Parameterized.class)
public class TestMyDefaultHandler2 {

    private Attributes attributes;
    private SAXReader saxReader;
    private File sourceFile;
    private boolean expect;

    public TestMyDefaultHandler2(Attributes attributes,boolean expect){
        this.attributes = attributes;
        this.expect = expect;
    }

    @Before
    public void setUp(){

        try {
            sourceFile = File.createTempFile("temp",".xml");
            sourceFile.deleteOnExit();
        }
        catch (IOException ex){
            fail();
        }

        saxReader = new SAXReader(sourceFile,new LinkedBlockingQueue<Attributes>());
    }


    @Test
    public void testValidateNodeAttributes() throws Exception {
        boolean result = saxReader.getDefaultHandler().validateNodeAttributes(attributes);

        assertEquals(expect,result);
    }


    @Parameterized.Parameters
    public static Collection attrList() {
        return Arrays.asList(createTestAttributesList());
    }

    private static Object[][] createTestAttributesList(){

        AttributesImpl attr1 = new AttributesImpl();
        attr1.addAttribute(null,null,"name",null,"a");
        attr1.addAttribute(null,null,"type",null,"root");
        attr1.addAttribute(null,null,"weight",null,"10");


        AttributesImpl attr2 = new AttributesImpl();
        attr2.addAttribute(null,null,"name",null,"b");
        attr2.addAttribute(null,null,"type",null,"child");
        attr2.addAttribute(null,null,"weight",null,"10");

        AttributesImpl attr3 = new AttributesImpl();
        attr3.addAttribute(null,null,"name",null,"c");
        attr3.addAttribute(null,null,"type",null,"sub");
        attr3.addAttribute(null,null,"weight",null,"10");

        AttributesImpl attr4 = new AttributesImpl();
        attr4.addAttribute(null,null,"name",null,"");
        attr4.addAttribute(null,null,"type",null,"root");
        attr4.addAttribute(null,null,"weight",null,"10");

        AttributesImpl attr5 = new AttributesImpl();
        attr5.addAttribute(null,null,"name",null,"d");
        attr5.addAttribute(null,null,"type",null,"test");
        attr5.addAttribute(null,null,"weight",null,"10");

        AttributesImpl attr6 = new AttributesImpl();
        attr6.addAttribute(null,null,"name",null,"e");
        attr6.addAttribute(null,null,"type",null,"root");
        attr6.addAttribute(null,null,"weight",null,"-1");

        AttributesImpl attr7 = new AttributesImpl();
        attr7.addAttribute(null,null,"name",null,"f");
        attr7.addAttribute(null,null,"type",null,"root");
        attr7.addAttribute(null,null,"weight",null,"");

        AttributesImpl attr8 = new AttributesImpl();
        attr8.addAttribute(null,null,"name",null,"f");
        attr8.addAttribute(null,null,"type",null,"");
        attr8.addAttribute(null,null,"weight",null,"10");

        AttributesImpl attr9 = new AttributesImpl();
        attr9.addAttribute(null,null,"type",null,"child");
        attr9.addAttribute(null,null,"weight",null,"10");

        AttributesImpl attr10 = new AttributesImpl();
        attr10.addAttribute(null,null,"name",null,"f");
        attr10.addAttribute(null,null,"weight",null,"10");

        AttributesImpl attr11 = new AttributesImpl();
        attr11.addAttribute(null,null,"name",null,"f");
        attr11.addAttribute(null,null,"type",null,"sub");


        return new Object[][]{
                              {attr1,true}, {attr2,true}, {attr3,true},
                              {attr4,false}, {attr5,false}, {attr6,false},
                              {attr7,false}, {attr8,false}, {attr9,false},
                              {attr10,false}, {attr11,false}
                                                                           };
    }
}