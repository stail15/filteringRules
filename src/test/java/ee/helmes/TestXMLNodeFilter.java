package ee.helmes;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogManager;

import static org.junit.Assert.*;


public class TestXMLNodeFilter {

    private XMLNodeFilter xmlNodeFilter;
    private File tempFile;

    @Before
    public void setUp() throws Exception {
        LinkedBlockingQueue<Attributes> attrList = new LinkedBlockingQueue<>();
        try {
            tempFile = File.createTempFile("temp",".xml");
            tempFile.deleteOnExit();
        }
        catch (IOException ex){
            fail();
        }
        SAXReader saxReader = new SAXReader(tempFile, attrList);
        xmlNodeFilter = new XMLNodeFilter(tempFile, saxReader);
    }


    @Test
    public void testApplyFilter() throws Exception{
        List<Attributes> list = createTestAttributesList();
        Map<String,Attributes> expectMap = createExpectedMap();
        for (Attributes attr : list){
            xmlNodeFilter.applyFilter(attr);
        }
        Map<String,Attributes> resultMap = xmlNodeFilter.getFilteredNodeMap();
        assertTrue(expectMap.equals(resultMap));

    }

    private static Map<String,Attributes> createExpectedMap(){
        Map<String,Attributes> attrMap = new HashMap<>();

        AttributesImplTest attr1 = new AttributesImplTest();
        attr1.addAttribute(null, null, "name", null, "a");
        attr1.addAttribute(null, null, "type", null, "child");
        attr1.addAttribute(null, null, "weight", null, "10");
        attrMap.put("a", attr1);

        AttributesImplTest attr2 = new AttributesImplTest();
        attr2.addAttribute(null, null, "name", null, "b");
        attr2.addAttribute(null, null, "type", null, "sub");
        attr2.addAttribute(null, null, "weight", null, "11");
        attrMap.put("b",attr2);

        AttributesImplTest attr3 = new AttributesImplTest();
        attr3.addAttribute(null, null, "name", null, "c");
        attr3.addAttribute(null, null, "type", null, "child");
        attr3.addAttribute(null, null, "weight", null, "9");
        attrMap.put("c", attr3);

        AttributesImplTest attr4 = new AttributesImplTest();
        attr4.addAttribute(null, null, "name", null, "f");
        attr4.addAttribute(null, null, "type", null, "");
        attr4.addAttribute(null, null, "weight", null, "10");
        attrMap.put("f", attr4);


        return attrMap;
    }
    private static List<Attributes> createTestAttributesList(){
        List<Attributes> attributesList = new ArrayList<>();

        AttributesImplTest attr1 = new AttributesImplTest();
        attr1.addAttribute(null,null,"name",null,"a");
        attr1.addAttribute(null,null,"type",null,"root");
        attr1.addAttribute(null,null,"weight",null,"10");
        attributesList.add(attr1);

        AttributesImplTest attr2 = new AttributesImplTest();
        attr2.addAttribute(null,null,"name",null,"a");
        attr2.addAttribute(null,null,"type",null,"child");
        attr2.addAttribute(null,null,"weight",null,"10");
        attributesList.add(attr2);

        AttributesImplTest attr3 = new AttributesImplTest();
        attr3.addAttribute(null,null,"name",null,"b");
        attr3.addAttribute(null,null,"type",null,"sub");
        attr3.addAttribute(null,null,"weight",null,"10");
        attributesList.add(attr3);

        AttributesImplTest attr4 = new AttributesImplTest();
        attr4.addAttribute(null,null,"name",null,"b");
        attr4.addAttribute(null,null,"type",null,"root");
        attr4.addAttribute(null,null,"weight",null,"10");
        attributesList.add(attr4);

        AttributesImplTest attr5 = new AttributesImplTest();
        attr5.addAttribute(null,null,"name",null,"b");
        attr5.addAttribute(null,null,"type",null,"sub");
        attr5.addAttribute(null,null,"weight",null,"11");
        attributesList.add(attr5);

        AttributesImplTest attr6 = new AttributesImplTest();
        attr6.addAttribute(null,null,"name",null,"c");
        attr6.addAttribute(null,null,"type",null,"child");
        attr6.addAttribute(null,null,"weight",null,"0");
        attributesList.add(attr6);

        AttributesImplTest attr7 = new AttributesImplTest();
        attr7.addAttribute(null,null,"name",null,"c");
        attr7.addAttribute(null,null,"type",null,"child");
        attr7.addAttribute(null,null,"weight",null,"9");
        attributesList.add(attr7);

        AttributesImplTest attr8 = new AttributesImplTest();
        attr8.addAttribute(null,null,"name",null,"f");
        attr8.addAttribute(null,null,"type",null,"");
        attr8.addAttribute(null,null,"weight",null,"10");
        attributesList.add(attr8);

        return attributesList;
    }

    private static class AttributesImplTest extends AttributesImpl{

        /**
         *  {@inheritDoc}
         * @param obj
         * @return
         */
        @Override
        public boolean equals(Object obj) {

            if(obj == null){
                return false;
            }

            if(!(obj instanceof AttributesImplTest)){
                return false;
            }

            if(obj == this){
                return true;
            }

            AttributesImplTest attrTest = (AttributesImplTest)obj;

            return this.getValue("name").equals(attrTest.getValue("name")) &&
                    this.getValue("type").equals(attrTest.getValue("type")) &&
                    this.getValue("weight").equals(attrTest.getValue("weight"));

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            String nameValue = this.getValue("name");
            String typeValue = this.getValue("type");
            String weightValue = this.getValue("weight");

            final int prime = 31;
            int result = 1;

            result = prime*result+(nameValue == null ? 0 : nameValue.hashCode());
            result = prime*result+(typeValue == null ? 0 : typeValue.hashCode());
            result = prime*result+(weightValue == null ? 0 : weightValue.hashCode());

            return result;
        }
    }
}