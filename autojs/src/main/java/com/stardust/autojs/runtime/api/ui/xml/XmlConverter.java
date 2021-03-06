package com.stardust.autojs.runtime.api.ui.xml;

import com.stardust.util.MapEntries;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Stardust on 2017/5/14.
 */

public class XmlConverter {

    private static final NodeHandler NODE_HANDLER = new NodeHandler.NameRouter()
            .handler("vertical", new NodeHandler.VerticalHandler("com.stardust.autojs.runtime.api.ui.widget.JsLinearLayout"))
            .defaultHandler(new NodeHandler.MapNameHandler()
                    .map("frame", "com.stardust.autojs.runtime.api.ui.widget.JsFrameLayout")
                    .map("linear", "com.stardust.autojs.runtime.api.ui.widget.JsLinearLayout")
                    .map("relative", "com.stardust.autojs.runtime.api.ui.widget.JsRelativeLayout")
                    .map("button", "com.stardust.autojs.runtime.api.ui.widget.JsButton")
                    .map("text", "com.stardust.autojs.runtime.api.ui.widget.JsTextView")
                    .map("input", "com.stardust.autojs.runtime.api.ui.widget.JsEditText")
                    .map("image", "ImageView")
            );

    private static final AttributeHandler ATTRIBUTE_HANDLER = new AttributeHandler.AttrNameRouter()
            .handler("w", new AttributeHandler.DimenHandler("width"))
            .handler("h", new AttributeHandler.DimenHandler("height"))
            .handler("size", new AttributeHandler.DimenHandler("textSize"))
            .handler("id", new AttributeHandler.IdHandler())
            .handler("vertical", new AttributeHandler.OrientationHandler())
            .handler("margin", new AttributeHandler.MarginPaddingHandler("layout_margin"))
            .handler("padding", new AttributeHandler.MarginPaddingHandler("padding"))
            .handler("marginLeft", new AttributeHandler.DimenHandler("layout_marginLeft"))
            .handler("marginRight", new AttributeHandler.DimenHandler("layout_marginRight"))
            .handler("marginTop", new AttributeHandler.DimenHandler("layout_marginTop"))
            .handler("marginBottom", new AttributeHandler.DimenHandler("layout_marginBottom"))
            .handler("paddingLeft", new AttributeHandler.DimenHandler("paddingLeft"))
            .handler("paddingRight", new AttributeHandler.DimenHandler("paddingRight"))
            .handler("paddingTop", new AttributeHandler.DimenHandler("paddingTop"))
            .handler("paddingBottom", new AttributeHandler.DimenHandler("paddingBottom"))
            .defaultHandler(new AttributeHandler.MappedAttributeHandler()
                    .mapName("align", "layout_gravity")
                    .mapName("bg", "background")
                    .mapName("color", "textColor")
            );

    public static String convertToAndroidLayout(String xml) throws IOException, SAXException, ParserConfigurationException {
        return convertToAndroidLayout(new InputSource(new StringReader(xml)));
    }

    public static String convertToAndroidLayout(InputSource source) throws ParserConfigurationException, IOException, SAXException {
        StringBuilder layoutXml = new StringBuilder();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(source);
        handleNode(document.getFirstChild(), "xmlns:android=\"http://schemas.android.com/apk/res/android\"", layoutXml);
        return layoutXml.toString();
    }

    private static void handleNode(Node node, String namespace, StringBuilder layoutXml) {
        String nodeName = node.getNodeName();
        String mappedNodeName = NODE_HANDLER.handleNode(node, namespace, layoutXml);
        handleText(nodeName, node.getTextContent(), layoutXml);
        handleAttributes(nodeName, node.getAttributes(), layoutXml);
        layoutXml.append(">\n");
        handleChildren(node.getChildNodes(), layoutXml);
        layoutXml.append("</").append(mappedNodeName).append(">\n");
    }

    private static void handleText(String nodeName, String textContent, StringBuilder layoutXml) {
        if (textContent == null || textContent.isEmpty()) {
            return;
        }
        if (nodeName.equals("text") || nodeName.equals("button") || nodeName.equals("input"))
            layoutXml.append("android:text=\"").append(textContent).append("\"\n");
    }

    private static void handleChildren(NodeList nodes, StringBuilder layoutXml) {
        if (nodes == null)
            return;
        int len = nodes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;
            handleNode(node, "", layoutXml);
        }
    }


    private static void handleAttributes(String nodeName, NamedNodeMap attributes, StringBuilder layoutXml) {
        if (attributes == null)
            return;
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
            Node attr = attributes.item(i);
            handleAttribute(nodeName, attr, layoutXml);
        }
    }

    private static void handleAttribute(String nodeName, Node attr, StringBuilder layoutXml) {
        ATTRIBUTE_HANDLER.handle(nodeName, attr, layoutXml);
    }


}
