package ee.helmes;

import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses input XML file and adds nodes attributes to the {@link java.util.concurrent.LinkedBlockingQueue} for further filtering.
 */

public class SAXReader implements Runnable {

    /**
     * {@code Logger} object that logs the program execution process.
     */
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     *  Handler for SAX2 events.<br>
     * {@link ee.helmes.SAXReader.MyDefaultHandler MyDefaultHandler} object extends {@link org.xml.sax.helpers.DefaultHandler}.
     */
    private final MyDefaultHandler defaultHandler = new MyDefaultHandler();

    /**
     *Queue for storing nodes from input XML file
     */
    private LinkedBlockingQueue<Attributes> attributesList;

    /**
     * Flag that reflects status of parsing input XML file.<br>
     * {@code false} if parsing of XML file is in progress, {@code true} if file was parsed.
     */
    private volatile boolean xmlFileIsParsed = false;

    /**
     * Flag that reflects whether the error occurred during parsing process.<br>
     * {@code true} if any error occurred while parsing file, {@code false} otherwise.
     */
    private volatile boolean errorWhileParse = false;

    /**
     * Input XML file for parsing.
     */
    private final File xmlFile;

    /**
     * Auxiliary variable which stores the name of the current {@link java.lang.Thread}.<br>
     * Is used in creating messages for {@link java.util.logging.Logger} object
     */
    private String threadName;

    /**
     * Initializes a newly created {@link ee.helmes.SAXReader} object.
     * @param xmlFile {@link java.io.File} object associated with the input source XML file
     * @param attrList queue for storing XML nodes from  the input source XML file
     */
    public SAXReader (File xmlFile,LinkedBlockingQueue<Attributes> attrList){
        if(xmlFile == null || attrList==null){
            throw new NullPointerException();
        }
        this.attributesList = attrList;
        this.xmlFile = xmlFile;

    }


    /**
     * Entry point for {@link Thread} associated with {@link ee.helmes.SAXReader} object.<br>
     * The goal of this method just to invoke {@link SAXReader#startParsing()} method,
     * which takes care of further execution of parsing.
     *
     */
    public void run(){

        this.startParsing();

    }

    /**
     * Execute XML parsing process.<br>
     *     Creates {@link org.xml.sax.XMLReader} object from {@link javax.xml.parsers.SAXParser},
     *     sets {@link ee.helmes.SAXReader.MyDefaultHandler MyDefaultHandler} object as
     *     for for SAX2 events.
     */
    public void startParsing() {
        try {
            threadName = Thread.currentThread().getName();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(defaultHandler);
            xmlReader.parse(new InputSource(new FileInputStream(xmlFile)));
        }
        catch (ParserConfigurationException | SAXException | NullPointerException |IOException ex){
            logger.log(Level.SEVERE,threadName+": current thread was stopped because of "+ex.toString(), ex);
            errorWhileParse = true;
        }
    }

    /**
     * Returns {@code attributesList} which is used to store nodes from input XML file.
     *
     * @return {@link ee.helmes.SAXReader#attributesList} which is used to store nodes from input XML file
     */
    public LinkedBlockingQueue<Attributes> getAttributesList() {
        return attributesList;
    }

    /**
     * Returns handler for SAX2 events.
     *
     * @return {@link ee.helmes.SAXReader#defaultHandler handler} for SAX2 events.
     */
    MyDefaultHandler getDefaultHandler() {
        return defaultHandler;
    }

    /**
     * Returns error status of parsing process.
     *
     * @return {@code true} if errors occurred while processing input XML file, {@code false} otherwise
     */
    public boolean isErrorWhileParse() {
        return errorWhileParse;
    }

    /**
     * Returns status of parsing process.
     *
     * @return {@code true} if XML file was parsed, {@code false} if parsing process is in progress.
     */
    public boolean isXmlFileIsParsed() {
        return xmlFileIsParsed;
    }

    /**
     * Implementation of {@link org.xml.sax.helpers.DefaultHandler} class for SAX2 events handlers.<br>
     * This class overrides {@link DefaultHandler#startDocument()},<br>
     *                      {@link org.xml.sax.helpers.DefaultHandler#startElement(String, String, String, Attributes)},<br>
     *                      {@link org.xml.sax.helpers.DefaultHandler#endElement(String, String, String)} and <br>
     *                      {@link DefaultHandler#endDocument()}
     *                       methods of parent class.
     */
    class MyDefaultHandler extends DefaultHandler {

        /**
         * Auxiliary variable for storing nodePath of XML elements.
         */
        private String nodePath;

        /**
         * Fires when the notification of the start of the document is received.<br>
         * This method initializes {@code nodePath} variable.
         *
         * @throws SAXException Any SAX exception, possibly wrapping another exception.
         * @see ContentHandler#startDocument()
         */
        @Override
        public void startDocument() throws SAXException {
            logger.info(threadName + ": XML file is being parsed...");
            nodePath = "/";

        }

        /**
         * Fires when the notification of the start of an element is received.<br>
         * This method builds the {@link ee.helmes.SAXReader.MyDefaultHandler#nodePath} of element and validates it.<br>
         * In case of valid nodePath, method validates attributes attached to element and
         * adds {@code attributes} object to {@link ee.helmes.SAXReader#attributesList} for further processing if attributes are valid.
         *
         *@param  uri - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
         *@param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
         *@param qName - The qualified name (with prefix), or the empty string if qualified names are not available.
         *@param attributes - The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
         *@throws SAXException Any SAX exception, possibly wrapping another exception.
         *@see ContentHandler#startElement(String, String, String, Attributes)
         */
        @Override
        public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
            nodePath = nodePath + localName + "/";

            if(this.validateNodePath(nodePath)){
                logger.info(threadName + ": - node path is valid;");
                if(this.validateNodeAttributes(attributes)) {
                    logger.info(threadName + ": - node attributes are valid;");
                    try {
                        Attributes attr = new AttributesImpl(attributes);
                        while (!attributesList.offer(attr, 10, TimeUnit.MICROSECONDS));
                        logger.info(threadName + ": - node was added to queue.");
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, threadName + ": The application execution is stopped because of the InterruptedException:", ex);
                    }
                }else {
                    logger.info(threadName + ": - node attributes are not valid;");
                }
            }else {
                logger.info(threadName + ": - node path is not valid;");
            }

        }

        /**
         * Fires when the notification of the end of an element is received.<br>
         *
         * This method builds the {@link ee.helmes.SAXReader.MyDefaultHandler#nodePath}.
         *
         *@param uri - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
         *@param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
         *@param qName - The qualified name (with prefix), or the empty string if qualified names are not available.
         *@throws SAXException Any SAX exception, possibly wrapping another exception.
         *@see ContentHandler#endElement(String, String, String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            String[] nodePathToArray = nodePath.split("/");
            StringBuilder nodeBuilder = new StringBuilder("/");
            int size = nodePathToArray.length;
            if(size>0){
                for(int i = 1; i<size-1;i++){
                    nodeBuilder.append(nodePathToArray[i])
                               .append("/");
                }
                nodePath = nodeBuilder.toString();
            }

        }

        /**
         * Fires when the notification of the end of the document is received.<br>
         *
         * Sets the variable {@link ee.helmes.SAXReader.MyDefaultHandler#xmlFileIsParsed} value to {@code true}.
         *
         *@throws SAXException Any SAX exception, possibly wrapping another exception.
         *@see ContentHandler#endDocument()
         */
        @Override
        public void endDocument() throws SAXException {
            logger.info(threadName + ": XML file was parsed.");
            xmlFileIsParsed = true;
        }


        /**
         * Returns the status of {@code nodePath} validation.
         *
         * @param nodePath string representation of the path to the element.
         * @return {@code true} if {@code nodePath} value is valid; returns {@code false} otherwise.
         */
        public boolean validateNodePath(String nodePath){
            if(nodePath!=null) {
                logger.info(threadName + ": Parsing node \"" + nodePath + "\":");
                String[] nodePathArray = nodePath.split("/");
                int length = nodePathArray.length;
                if ( length > 2) {
                    nodePath = nodePathArray[length - 2] + "/" + nodePathArray[length - 1];
                }
            }
            String correctNodePath = "rules/rule";
            return correctNodePath.equals(nodePath);
        }

        /**
         * Returns the validation status of attributes attached to the current element.<br>
         *     Validates element according to following rules:<br>
         *          - element contains {@code name} attribute with {@code not null} value;<br>
         *          - element contains {@code type} attribute with {@code "child","sub"} or {@code "root"} value;<br>
         *          - element contains {@code weight} attribute with value of positive integer.
         *
         * @param attributes attributes of currently parsed element of the XML file.
         *
         * @return {@code true} if element's attributes are valid; returns {@code false} otherwise.
         */
        protected boolean validateNodeAttributes(Attributes attributes){
            boolean valid = false;
            String nodeNameValue = attributes.getValue(RuleAttr.NAME.getValue());

            if(nodeNameValue!=null){

                if(nodeNameValue.length()>0) {

                    String nodeTypeValue = attributes.getValue(RuleAttr.TYPE.getValue());

                    if (nodeTypeValue!= null) {

                        if(NodeType.getType(nodeTypeValue)!=0){

                            String nodeWeightValue = attributes.getValue(RuleAttr.WEIGHT.getValue());

                            if (nodeWeightValue!= null) {
                                try {
                                    Integer weightValue = Integer.parseInt(nodeWeightValue);
                                    if(weightValue>0){
                                        valid = true;
                                    }
                                    else {
                                        logger.info(threadName + ": - invalid \"weight\" attribute value - it must be a positive integer.");
                                    }
                                }
                                catch (NumberFormatException ex){
                                    logger.info(threadName + ": - invalid \"weight\" attribute value - java.lang.NumberFormatException.");
                                }

                            }
                            else {

                                logger.info(threadName + ": - there is no \"weight\" attribute in current node.");

                            }
                        }
                        else {
                            logger.info(threadName + ": - invalid \"type\" attribute value.");
                        }
                    }
                    else {
                        logger.info(threadName + ": - there is no \"type\" attribute in current node.");
                    }
                }else {
                    logger.info(threadName + ": - empty \"name\" attribute value.");
                }
            }else {
                logger.info(threadName + ": - there si no \"name\" attribute in current node.");
            }
            return valid;
        }
    }

}
