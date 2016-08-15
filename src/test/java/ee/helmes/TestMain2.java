package ee.helmes;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;



@RunWith(Parameterized.class)
public class TestMain2 {

    private String[]args;
    private boolean expect;

    public TestMain2(String[] args, boolean expect){
        this.args = args;
        this.expect = expect;
    }


    @Test
    public void testCheckInputArgs() {
        boolean result = Main.validateInputArgs(args);

        assertEquals(expect,result);
    }

    @Parameterized.Parameters
    public static Collection validArgs() {
        return Arrays.asList(new Object[][]{
                {null,false},
                {new String[]{null,null},false},
                {new String[]{"",null},false},
                {new String[]{null,""},false},
                {new String[]{"",""},false}
        });
    }

}
