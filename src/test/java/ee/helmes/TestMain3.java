package ee.helmes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;


@RunWith(Parameterized.class)
public class TestMain3 {

    private String fileName;
    private boolean expect;

    public TestMain3(String fileName, boolean expected){
        this.fileName = fileName;
        this.expect = expected;
    }

    @Test
    public void testIsXMLFile() throws Exception {
        boolean result = Main.isXMLFile(fileName);

        assertEquals(expect,result);

    }

    @Parameterized.Parameters
    public static Collection validArgs() {
        return Arrays.asList(new Object[][]{
                {null, false},
                {"", false},
                {"xml.", false},
                {"0.txt", false},
                {".xml", false},
                {"0.xml", true},
                {"0.XML", true},
                {"0.xmL", true},
                {"0. xmL", false},
                {"test.xmls", false},
                {". xml", false}
        });
    }
}