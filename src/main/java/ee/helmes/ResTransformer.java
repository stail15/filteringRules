package ee.helmes;

import org.w3c.dom.Document;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transforms the result of filtering to XML and HTML files.<br>
 *     Creates XML file from and saves the result of parsing and filtering to it.<br>
 *     Produces HTML file from result XML file by applying XSLT template to it.
 */
class ResTransformer {


    /**
     * {@code Logger} object that logs the program execution process.
     */
    private static final Logger logger = Logger.getLogger(ResTransformer.class.getName());

    /**
     * Auxiliary variable which stores the name of the current {@link java.lang.Thread}.<br>
     * Is used in creating messages for {@link java.util.logging.Logger} object
     */
    private final String threadName;

    /**
     * {@code Documet} object which represents the result of parsing and filtering.
     */
    private Document resultDocument;


    /**
     * Initializes a newly created {@link ee.helmes.XMLNodeFilter} object.
     *
     * @param resultDocument {@link org.w3c.dom.Document} object.
     */
    ResTransformer(Document resultDocument){
        this.resultDocument = resultDocument;
        this.threadName = Thread.currentThread().getName();
    }


    /**
     * Transforms results of filtering rules from {@link org.w3c.dom.Document} object to XML and HTML files.
     *
     * @param resultXml {@link java.io.File} object for saving results in it.
     */
    public void performTransformation(File resultXml) {
        TransformerFactory factory = TransformerFactory.newInstance();
        DOMSource xmlSource = new DOMSource(resultDocument);

        if(this.saveResultToFile(resultXml, xmlSource,factory)){
            logger.info(threadName + ": - the result document was successfully saved to "+resultXml.getAbsolutePath()+".");
        }

        File resultHTML = this.produceHTML(xmlSource,factory);

        if(resultHTML!=null){
            this.displayResultFile(resultHTML);
        }

    }

    /**
     * Returns {@code true} if result from {@link ResTransformer#resultDocument}
     * was saved in XML form to {@code resultXML} file, {@code false} otherwise.
     *
     * @param resultXml {@link java.io.File} object for saving results in it.
     *
     * @param xmlSource {@link javax.xml.transform.dom.DOMSource} object created
     *
     *                  from {@link ResTransformer#resultDocument}.
     * @param factory {@link javax.xml.transform.TransformerFactory} object to produce {@link javax.xml.transform.Transformer}.
     *
     * @return {@code true} if result from {@link ResTransformer#resultDocument}
     * was saved in XML form to {@code resultXML} file, {@code false} otherwise.
     */
    protected boolean saveResultToFile(File resultXml, DOMSource xmlSource, TransformerFactory factory){
        boolean saved = false;
        Transformer xmlTransformer;

        try {
            xmlTransformer= factory.newTransformer();
            Result resultToFile = new StreamResult(resultXml);
            xmlTransformer.transform(xmlSource,resultToFile);
            saved = true;
        }
        catch (TransformerConfigurationException ex){
            logger.log(Level.WARNING,threadName + ": the result document wasn't saved to file "+resultXml.getAbsolutePath()+" because of the TransformerConfigurationException",ex);
        }
        catch (TransformerException ex){
            logger.log(Level.WARNING,threadName + ": the result document wasn't saved to file "+resultXml.getAbsolutePath()+" because of the TransformerException",ex);
        }
        catch (RuntimeException ex){
            logger.log(Level.WARNING,threadName + ": the result document wasn't saved to file "+resultXml.getAbsolutePath()+" because of the RuntimeException",ex);
        }
        catch (Exception ex){
            logger.log(Level.WARNING,threadName + ": the result document wasn't saved to file "+resultXml.getAbsolutePath()+" because of the Exception",ex);
        }

        return saved;
    }


    /**
     * Returns {@link java.io.File} object which stores the result of applying XSLT template
     * to {@link javax.xml.transform.dom.DOMSource} in HTML format.
     *
     * @param xmlSource {@link javax.xml.transform.dom.DOMSource} object
     *                   created from {@link ResTransformer#resultDocument}.
     *
     * @param factory {@link javax.xml.transform.TransformerFactory} object to produce
     *                {@link javax.xml.transform.Transformer}.
     *
     * @return {@link java.io.File} object which stores the result of applying XSLT template
     * to {@link javax.xml.transform.dom.DOMSource} in HTML format.
     */
    protected File produceHTML(DOMSource xmlSource, TransformerFactory factory){
        File resultHtml = null;
        try {

            logger.info(threadName + ": - creating temporary HTML file...");
            resultHtml = File.createTempFile("result", ".html");
            Result resultToHtml = new StreamResult(resultHtml);

            logger.info(threadName + ": - loading XSLT file from resources...");
            Source source = new StreamSource(this.getClass().getResourceAsStream("/stylesheet.xsl"));

            Transformer xmlTransformer = factory.newTransformer(source);
            logger.info(threadName + ": - applying XSLT to result XML file...");
            xmlTransformer.transform(xmlSource, resultToHtml);

        }

        catch (TransformerException | RuntimeException |IOException ex){
            logger.log(Level.WARNING, threadName
                                     + ":  - the result file can not be displayed in HTML because of the "
                                     + ex.getMessage()
                                     + ":", ex);
        }

        return resultHtml;
    }

    /**
     * Opens the result HTML file in the default browser, if the current JVM supports this action.
     *
     * @param resultHtml {@link java.io.File} object in HTML format.
     */
    private void displayResultFile(File resultHtml){

       try {
           if (resultHtml!=null){
               Desktop.getDesktop().browse(resultHtml.toURI());
           }
       }
       catch (UnsupportedOperationException | IOException ex){
           logger.log(Level.WARNING, threadName
                   + ":  - the result file can not be displayed in HTML because of the "
                   + ex.getMessage()
                   + ":", ex);
       }

        logger.info(threadName + ": - the result XML file was successfully displayed in HTML.");
    }


}
