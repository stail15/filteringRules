package ee.helmes;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Filters nodes attributes from the {@link java.util.concurrent.LinkedBlockingQueue}.
 */


public class XMLNodeFilter implements Runnable {

    /**
     *
     * {@code Logger} object that logs the program execution process.
     */
    private static final Logger logger = Logger.getLogger(XMLNodeFilter.class.getName());

    /**
     * {@link java.util.Map} implemented object for storing results of filtering.<br>
     *     All results are stored in {@code <key, value>} pairs,
     *     where {@code name} attribute value is a key and {@link org.xml.sax.Attributes} object is a value.
     */
    private final Map<String, Attributes> filteredNodeMap = new HashMap<>();

    /**
     * {@code SAXReader} object for checking error and progress statuses while parsing.
     */
    private final SAXReader saxReader;

    /**
     * Queue object which stores valid nodes attributes from input XML file.
     */
    private final LinkedBlockingQueue<Attributes> attributesList;

    /**
     *{@code Attributes} object for node attributes from {@code attributesList}.
     */
    private Attributes attributes;

    /**
     * Auxiliary variable which stores the name of the current {@link java.lang.Thread}.<br>
     * Is used in creating messages for {@link java.util.logging.Logger} object
     */
    private String threadName;

    /**
     * {@code File} object for storing results of filtering in XML form.
     */
    private final File resultXml;


    /**
     * Initializes a newly created {@link ee.helmes.XMLNodeFilter} object.
     *
     * @param resultXml {@link java.io.File} object for storing results of filtering.
     * @param saxReader {@link ee.helmes.SAXReader} object which parses input sourse XML file.
     */
    public XMLNodeFilter(File resultXml,SAXReader saxReader){
        if(resultXml ==null || saxReader == null){
            throw new NullPointerException();
        }
        this.saxReader = saxReader;
        this.attributesList = saxReader.getAttributesList();
        this.resultXml = resultXml;
    }

    /**
     * Entry point for {@link Thread} associated with {@link ee.helmes.XMLNodeFilter} object.<br>
     * The goal of this method just to invoke {@link XMLNodeFilter#startFiltering()} method,
     * which takes care of further filtering attributes from Queue.
     *
     */
    public void run() {

        this.startFiltering();

    }

    /**
     * Execute filtering process.<br>
     *     Gets {@code attributes} from the Queue (till it stores elements in it
     *     and input XML file is not fully parsed) and applies filter to them .<br>
     *     If any error occurred during parsing source file, method stops getting
     *     {attributes} from Queue.<br>
     *     All {@code attributes} objects from Queue are filtered according to rules and
     *     those of them, which passed filter, are added to the result Map object. <br>
     *     When the Queue object is empty ant there woun't be and {@code attributes} in it
     *     or error occurred while parsing file, method starts the process of transforming
     *     the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}
     *     object to the result XML and HTML files.
     */
    public void startFiltering() {
        threadName = Thread.currentThread().getName();

        logger.info(threadName + ": Node filtering is being started...");
        while (!saxReader.isXmlFileIsParsed()||!attributesList.isEmpty()) {
            try {
                attributes = attributesList.poll(10, TimeUnit.MICROSECONDS);
            }
            catch (InterruptedException ex){ex.printStackTrace();}


            if(attributes!=null){

                this.applyFilter(attributes);

            }

            if(saxReader.isErrorWhileParse()){
                break;
            }
        }
        logger.info(threadName + ": Node filtering was successfully completed.");

        Document resultDocument = this.createResultDocument();

        ResTransformer resTransformer = new ResTransformer(resultDocument);
        resTransformer.performTransformation(resultXml);

        logger.info(threadName + ": - the result document was successfully created.");
    }

    /**
     * Returns the result of applying filter to the node attributes.<br>
     *     Checks whether the {@code filteredNodeMap} contains object with
     *     the given {@code "name"} attribute value.<br>
     *
     *     If map does not contain such object, adds object to the map.<br>
     *
     *     If an object with such {@code "name"} attribute value contained in map,
     *     compares {@code "type"} attribute values of objects from the {@code filteredNodeMap} and
     *     input parameter.<br>
     *
     *     If the {@code "type"} attribute value from the input object is higher than the other one from the {@code filteredNodeMap},
     *     it replaces the object from the map with the given object.<br>
     *
     *     If the {@code "type"} attribute value of objects are equal,
     *     it compares {@code "weight"} attribute values of this objects.
     *     Object with the higher integer value wins and is saved to the place map.
     *
     *
     * @param attributes {@link Attributes} object to filter.
     * @return {@code true} if node passed the filter and was added
     * to the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}; {@code false} otherwise.
     */
    public boolean applyFilter(Attributes attributes) {
        String nodeName = attributes.getValue(RuleAttr.NAME.getValue());
        String nodeType = attributes.getValue(RuleAttr.TYPE.getValue());
        String nodeWeight = attributes.getValue(RuleAttr.WEIGHT.getValue());
        String nodeToString = XMLNodeFilter.getNodeDescription(attributes);

        boolean insertToMap = false;
        if (filteredNodeMap.containsKey(nodeName)) {
            Attributes attributesFromMap = filteredNodeMap.get(nodeName);
            String nodeTypeFromMap = attributesFromMap.getValue(RuleAttr.TYPE.getValue());
            String nodeFromMapToString = XMLNodeFilter.getNodeDescription(attributesFromMap);

            if (ruleTypeIsHigher(nodeType, nodeTypeFromMap)) {
                insertToMap = true;
                logger.info(threadName + ": - node " + nodeFromMapToString + " will be replaced with the higher rule.");
            } else {
                if (ruleTypeIsEqual(nodeType, nodeTypeFromMap)) {
                    String nodeWeightFromMap = attributesFromMap.getValue(RuleAttr.WEIGHT.getValue());
                    if (ruleWeightIsBigger(nodeWeight, nodeWeightFromMap)) {
                        insertToMap = true;
                        logger.info(threadName + ": - node " + nodeFromMapToString + " will be replaced with  the higher rule.");
                    }
                }
            }
        } else {
            insertToMap = true;
        }

        if(insertToMap){
            filteredNodeMap.put(nodeName, attributes);
            logger.info(threadName + ": - node " + nodeToString + " was added to the result list.");
        }
        else {
            logger.info(threadName + ": - node " + nodeToString + " was rejected.");
        }
        return insertToMap;
    }

    /**
     * Returns the {@link java.lang.String string} representation of {@code attributes}.
     *
     * This method is used by {@code logger} object during logging messages.
     *
     * @param attributes object from the {@link ee.helmes.XMLNodeFilter#attributesList}
     *
     * @return the {@link java.lang.String string} representation of {@code attributes}
     */
    private static String getNodeDescription(Attributes attributes){
        String nodeName = attributes.getValue(RuleAttr.NAME.getValue());
        String nodeType = attributes.getValue(RuleAttr.TYPE.getValue());
        String nodeWeight = attributes.getValue(RuleAttr.WEIGHT.getValue());

        return String.format("<rule name=\"%s\" type=\"%s\" weigh=\"%s\"/>",nodeName, nodeType, nodeWeight);
    }


    /**
     * Creates {@link org.w3c.dom.Document} object from {@link ee.helmes.XMLNodeFilter#filteredNodeMap}
     *
     * @return {@link org.w3c.dom.Document} object or {@code null} if any exception occurred;
     */
    private Document createResultDocument(){
        Document resultDocument = null;
        logger.info(threadName + ": Creating result document:");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;


        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException ex){
            logger.log(Level.SEVERE, threadName + ": - the result document wasn't created:");
            logger.log(Level.SEVERE, threadName + ": the application execution is stopped because of the ParserConfigurationException", ex);
            return null;
        }
        if(documentBuilder!=null){
            resultDocument = documentBuilder.newDocument();
            Element rootElement = resultDocument.createElement("rules");
            resultDocument.appendChild(rootElement);

            for(Map.Entry<String, Attributes> entry : filteredNodeMap.entrySet()){
                attributes = entry.getValue();

                String name = attributes.getValue(RuleAttr.NAME.getValue());
                String type = attributes.getValue(RuleAttr.TYPE.getValue());
                String weight = attributes.getValue(RuleAttr.WEIGHT.getValue());

                Element rule = resultDocument.createElement("rule");
                rule.setAttribute(RuleAttr.NAME.getValue(),name);
                rule.setAttribute(RuleAttr.TYPE.getValue(),type);
                rule.setAttribute(RuleAttr.WEIGHT.getValue(),weight);
                rootElement.appendChild(rule);
            }

        }
        return resultDocument;
    }

    /**
     * Returns {@code true} if the {@code "type"} attribute value from the {@link ee.helmes.XMLNodeFilter#attributesList}
     * is higher then the other one from the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}, {@code false} otherwise.
     *
     * @param nodeType string representation of the {@code "type"} attribute value from the {@link ee.helmes.XMLNodeFilter#attributesList}.

     * @param nodeTypeFromMap string representation of the {@code "type"} attribute value from the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}.
     * @return {@code true} if {@code "type"} attribute value from {@link ee.helmes.XMLNodeFilter#attributesList}
     * is higher then the other one from {@link ee.helmes.XMLNodeFilter#filteredNodeMap}, {@code false} otherwise.
     */
    private boolean ruleTypeIsHigher(String nodeType,String nodeTypeFromMap){
        return NodeType.getType(nodeType) > NodeType.getType(nodeTypeFromMap);
    }

    /**
     * Returns {@code true} if {@code "type"} attribute values from {@link ee.helmes.XMLNodeFilter#attributesList}
     * and {@link ee.helmes.XMLNodeFilter#filteredNodeMap} are equal, {@code false} otherwise.
     *
     * @param nodeType string representation of the {@code "type"} attribute value from the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}.
     * @param nodeTypeFromMap string representation of the {@code "type"} attribute value from the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}.
     * @return {@code true} if {@code "type"} attribute values from {@link ee.helmes.XMLNodeFilter#attributesList}
     * and {@link ee.helmes.XMLNodeFilter#filteredNodeMap} are equal, {@code false} otherwise.
     */
    private boolean ruleTypeIsEqual(String nodeType,String nodeTypeFromMap){
        return NodeType.getType(nodeType) == NodeType.getType(nodeTypeFromMap);
    }

    /**
     * Returns {@code true} if the {@code "weight"} attribute value from {@link ee.helmes.XMLNodeFilter#attributesList}
     * is bigger then the other one from the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}, {@code false} otherwise.
     *
     * @param nodeWeight string representation of the {@code "weight"} attribute value from the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}.
     * @param nodeWeightFromMap string representation of the {@code "weight"} attribute value from the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}.
     * @return {@code true} if the {@code "weight"} attribute value from {@link ee.helmes.XMLNodeFilter#attributesList}
     * is bigger then the other one from the {@link ee.helmes.XMLNodeFilter#filteredNodeMap}, {@code false} otherwise.
     */
    private boolean ruleWeightIsBigger(String nodeWeight, String nodeWeightFromMap){
        Integer weight = Integer.parseInt(nodeWeight);
        Integer weightFromMap = Integer.parseInt(nodeWeightFromMap);

        return weight>weightFromMap;
    }


    /**
     * Returns {@link java.util.Map} implemented object which stores the result of filtering {@code attributes} from {@link ee.helmes.XMLNodeFilter#filteredNodeMap}.
     * @return {@link java.util.Map} implemented object which stores the result of filtering {@code attributes} from {@link ee.helmes.XMLNodeFilter#filteredNodeMap}.
     */
    public Map<String, Attributes> getFilteredNodeMap() {
        return filteredNodeMap;
    }
}