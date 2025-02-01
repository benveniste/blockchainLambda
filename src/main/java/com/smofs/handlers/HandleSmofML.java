package com.smofs.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.smofs.blockchain.PeerChannel;
import org.hyperledger.fabric.sdk.Channel;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class HandleSmofML implements RequestHandler<SmofML, String> {
    @Override
    public String handleRequest(SmofML wrapper, Context context) {
        LambdaLogger logger = context.getLogger();
        PeerChannel peerChannel = new PeerChannel(logger);

        InputSource src = new InputSource(new StringReader(wrapper.uglyXML()));
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            Writer out = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(out));
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException oops) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos)) {
                oops.printStackTrace(ps);
                logger.log(oops.getMessage() + System.lineSeparator() + baos.toString(StandardCharsets.UTF_8));
            } catch (IOException inconceivable) {
                logger.log(oops.getMessage());
            }
            throw new RuntimeException(oops);
        }
        Channel channel = peerChannel.wire();
        return "Got here!";
    }
}

record SmofML(String uglyXML) {
}
