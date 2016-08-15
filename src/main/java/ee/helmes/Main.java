package ee.helmes;

import org.xml.sax.Attributes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;


/**
 * The entry point of the program.<br>
 * In this class program creates and initializes all main objects needed for the execution of the program.
 */
public class Main {

    /**
     * {@link Logger} object that logs the program execution process.
     */
    private static final Logger logger = Logger.getLogger(Main.class.getName());


    /**
     * Initializes a newly created {@link ee.helmes.Main} object.<br>
     * Note that use of this constructor is
     * unnecessary because all methods in this class are {@code static}.
     */
    public Main(){}

    /**
     * The entry point of the program.<br>
     * Method does the following:<br>
     * -configures {@link java.util.logging.LogManager LogManager};<br>
     * -initializes two {@link java.io.File Files} associated with input arguments;<br>
     * -creates two threads: one for parsing input XML file and the second for filtering rules from this file;<br>
     * -runs the threads and waits for their completion.
     *
     * @param args  {@link java.lang.String}[] object with absolute pathname of source and result XML files.
     *
     * @throws IllegalArgumentException if input arguments are not valid.
     * @throws IOException if {@link java.io.File} associated with result XML file does not exist and method failed to create a new one.
     * @throws InterruptedException if threads, that execute parsing XML file and filtering rules were interrupted.
     */
    public static void main(String[] args) throws IOException,IllegalArgumentException,InterruptedException{

        Main.configLogger("/logging.properties");

        logger.info(": Starting the application...");
        logger.info(": Checking input arguments:");

        if(!Main.validateInputArgs(args)){
            logger.severe(": Application execution is stopped because of invalid input arguments:");
            throw new IllegalArgumentException("Invalid input arguments");
        }
        else {
            if(!Main.isXMLFile(args[0]) & !Main.isXMLFile(args[1])){
                logger.severe(": Application execution is stopped because of the illegal file extension.");
                throw new IllegalArgumentException("Illegal file extension.");
            }
        }

        File sourceFile;
        File resultFile;

        try {
            sourceFile = Main.getSourceFile(args[0]);
            resultFile = Main.getResultFile(args[1]);
        }
        catch (FileNotFoundException ex){
            logger.log(Level.SEVERE,": Invalid input args[0] - file " + args[0] + " doesn't exist.",ex);
            throw ex;
        }
        catch (IOException ex){
            logger.log(Level.SEVERE,ex.getMessage(),ex);
            throw ex;
        }

        final LinkedBlockingQueue<Attributes> attributesList = new LinkedBlockingQueue<>();
        SAXReader saxReader = new SAXReader(sourceFile,attributesList);
        XMLNodeFilter xmlNodeFilter = new XMLNodeFilter(resultFile,saxReader);

        Thread parsingThread =  Main.createParsingThread(saxReader);
        Thread filteringThread = Main.createFilteringThread(xmlNodeFilter);

        parsingThread.start();
        filteringThread.start();



        try {

            parsingThread.join();
            filteringThread.join();
        }
        catch (InterruptedException ex){
            logger.log(Level.SEVERE,": Application execution is stopped because of the InterruptedException", ex);
            throw ex;
        }


        logger.info(": Filtering of rules was completed.");

    }

    /**
     * Returns {@code true} if {@link java.util.logging.LogManager LogManager} was successfully configured; returns false otherwise.<br>
     * Configures LogManager object
     * in accordance with file "logging.properties" in classpath.
     *
     * @param propertiesFile the string representation of the relative path to the {@code *.properties} file
     *                                     to configure {@link java.util.logging.LogManager LogManager}.
     * @return boolean {@code true} if LogManager was successfully configured; returns {@code false} otherwise.
     */
    public static boolean configLogger(String propertiesFile){
        boolean configured = false;
        try {

            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream(propertiesFile));
            configured = true;

        } catch (NullPointerException ex) {
            System.err.println(": Failed to configure LogManager - file \"logging.properties\" does not exist:");
            ex.printStackTrace();
        } catch (IOException ex){
            System.err.println(": LogManager failed to read properties file \"logging.properties\":");
            ex.printStackTrace();
        } catch (SecurityException ex){
            System.err.println(": LogManager does not have LoggingPermission(\"control\"):");
            ex.printStackTrace();
        }
        return configured;
    }

    /**
     * Creates {@link java.lang.Thread} object associated with {@link ee.helmes.SAXReader} object.
     * This thread is responsible for the process of parsing the input XML file.
     *
     * @param saxReader {@link ee.helmes.SAXReader} object.
     *
     * @return {@link java.lang.Thread} object that responsible for the process of parsing the input XML file.
     */
    static Thread createParsingThread(SAXReader saxReader){
        logger.info(": Creating thread for parsing input XML file...");
        Thread parsing_thread = new Thread(saxReader,"PARSING_THREAD");

        return parsing_thread;
    }


    /**
     * Creates {@link java.lang.Thread} object associated with {@link ee.helmes.XMLNodeFilter} object.<br>
     * This thread is responsible for the process of filtering {@link org.xml.sax.Attributes} from
     * {@link java.util.concurrent.LinkedBlockingQueue}.
     *
     * @param xmlNodeFilter {@link ee.helmes.XMLNodeFilter} object.
     *
     * @return {@link java.lang.Thread} object that responsible for the process of filtering  {@link org.xml.sax.Attributes}.
     */
    static Thread createFilteringThread(XMLNodeFilter xmlNodeFilter){
        logger.info(": Creating thread for filtering XML nodes...");

        Thread filterThread = new Thread(xmlNodeFilter,"FILTER__THREAD");


        return filterThread;
    }


    /**
     * Validates input arguments with which the program was started.
     * Checks whether the {@code args} is {@code not null}
     * or {@code args[0]} and {@code args[1]} are {@code not null}
     * or {@code args[0].length} and {@code args[1].length} is not less then 5 (without leading and trailing whitespace )
     * (min 1 character fo file name + 1 character for dot + 3 characters for file extension).
     *
     * @param args {@link java.lang.String}[] object with input parameters.
     *
     * @return {@code true} if input argumets are valid; returns {@code false} otherwise.
     */
    static boolean validateInputArgs(String[] args){

        boolean valid = false;

        if(args!=null && args.length>=2 && args[0]!=null && args[1]!=null){
            args[0]=args[0].trim();
            args[1]=args[1].trim();

            if(args[0].length()>=5 && args[1].length()>=5){
                valid = true;
            }
        }

        return valid;
    }

    /**
     * Return new {@link java.io.File} object associated with the given pathname of the source XML file.
     *
     * @param fileName absolute pathname of the source XML file.
     *
     * @return new {@link java.io.File} object associated with the given pathname of the source XML file.
     *
     * @throws FileNotFoundException if file associated with the given pathname does not exist.
     */
    static File getSourceFile(String fileName) throws FileNotFoundException{
        File sourceFile = new File(fileName);

        if(!sourceFile.isFile()){
            throw new FileNotFoundException("Input source file " + fileName + " doesn't exist.");
        }

        return sourceFile;
    }

    /**
     * Return new {@link java.io.File} object associated with the given pathname of the result file
     * for storing results of filtering rules. Checks whether the result file exists and tries to create
     * new file if it does not.
     *
     * @param fileName absolute pathname of the result XML file.
     *
     * @return new {@link java.io.File} object associated with the given pathname of the result XML file.
     *
     * @throws IOException if file does not exist and method failed to create a new one.
     */
    static File getResultFile(String fileName) throws IOException{
        File resultFile = new File(fileName);

        if(!resultFile.exists()){
            logger.info(": Invalid input args[1] - file " + resultFile.getAbsolutePath() + " doesn't exist.");
            logger.info(": Creating file " + resultFile.getAbsolutePath() + "...");

            resultFile.getParentFile().mkdirs();
            resultFile.createNewFile();
            if(!resultFile.exists()){
                throw new IOException("Failed to create result file " + fileName + ".");
            }
        }
        return resultFile;
    }


    /**
     * Returns {@code true} if the last chars sequence of given string is ".xml", {@code false} otherwise.
     *
     * @param fileName {@link java.lang.String} object that represents path to file.
     * @return {@code true} if the last chars sequence of given string is ".xml", {@code false} otherwise.
     */
    static boolean  isXMLFile(String fileName){
        boolean isXml = false;
        if(fileName == null || fileName.length()<5){
            return isXml;
        }
        int dotIndex = fileName.lastIndexOf(".");
        if(dotIndex!=-1 && (fileName.length()-1-dotIndex)==3){
            String extension = fileName.substring(dotIndex+1);
            if("xml".equalsIgnoreCase(extension)){
                isXml = true;
            }
        }
        return isXml;
    }

}
