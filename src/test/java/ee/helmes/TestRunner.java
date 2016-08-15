package ee.helmes;


import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


public class TestRunner {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestMain1.class,
                                             TestMain2.class,
                                             TestMain3.class,
                                             TestMyDefaultHandler1.class,
                                             TestMyDefaultHandler2.class,
                                             TestResultTransformer.class,
                                             TestSAXReader.class,
                                             TestXMLNodeFilter.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println(result.wasSuccessful());
    }

}