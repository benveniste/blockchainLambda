package blockchain;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class HandleSmofML implements RequestHandler<SmofML, String> {
    @Override
    public String handleRequest(SmofML wrapper, Context context) {
        try {
            InputSource src = new InputSource(new StringReader(wrapper.uglyXML()));
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            Writer out = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            HFCAClient fabricClient = new FabricClient().getClient(context.getLogger());
            return "Got here!";
        } catch (IOException | ParserConfigurationException | TransformerException | SAXException |
                 RuntimeException oops) {
            return oops.getMessage();
        }
    }
}

record SmofML(String uglyXML) {
}
