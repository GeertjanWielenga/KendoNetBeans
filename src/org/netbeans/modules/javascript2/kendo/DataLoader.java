package org.netbeans.modules.javascript2.kendo;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.markdown4j.Markdown4jProcessor;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Geertjan Wielenga
 */
public class DataLoader extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(DataLoader.class.getName());

    private static Map<String, Collection<KendoUIDataItem>> result = new HashMap<String, Collection<KendoUIDataItem>>();

    private static HTMLDocument htmlDoc = new HTMLDocument();

    public static Map<String, Collection<KendoUIDataItem>> getData(List<File> files) {
        result.clear();
        for (File file : files) {
            try {
                String html = new Markdown4jProcessor().process(file);
                System.out.println("html = " + html);
                try {
                    long start = System.currentTimeMillis();
//                SAXParserFactory factory = SAXParserFactory.newInstance();
//                SAXParser parser = factory.newSAXParser();
                    HTMLEditorKit.Parser parser = new HTMLParse().getParser();
                    htmlDoc.setParser(parser);
                    parser.parse(new StringReader(html), new HTMLParseLister(html), true);
//                DefaultHandler handler = new DataLoader();
//                parser.parse(file, handler);
                    long end = System.currentTimeMillis();
                    LOGGER.log(Level.FINE, "Loading data from file took {0}ms ", (end - start)); //NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
//            } catch (ParserConfigurationException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (SAXException ex) {
//                Exceptions.printStackTrace(ex);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }

    static class HTMLParseLister extends HTMLEditorKit.ParserCallback {

        private boolean inHeader = false;
        int startPos = -1;
        private int level = 0;
        private final String html;
        private static String lineSeparator
                = System.getProperty("line.separator", "\r\n");
        private char[] text;

        private HTMLParseLister(String html) {
            this.html = html;
        }

        @Override
        public void handleStartTag(HTML.Tag tag,
                MutableAttributeSet attributes, int position) {
            int newLevel = 0;
            if (tag == HTML.Tag.H1) {
                newLevel = 1;
            } else if (tag == HTML.Tag.H2) {
                newLevel = 2;
            } else if (tag == HTML.Tag.H3) {
                newLevel = 3;
            } else if (tag == HTML.Tag.H4) {
                newLevel = 4;
            } else if (tag == HTML.Tag.H5) {
                newLevel = 5;
            } else if (tag == HTML.Tag.H6) {
                newLevel = 6;
            } else if (tag == HTML.Tag.PRE) {
                newLevel = 7;
            } else {
                return;
            }
            this.inHeader = true;
//            if (newLevel > this.level) {
//                for (int i = 0; i < newLevel - this.level; i++) {
//                    if (level == 7) {
//                        System.out.print(newLevel + ": ");
//                    }
//                }
//            } else if (newLevel < this.level) {
////                for (int i = 0; i < this.level - newLevel; i++) {
////                    System.out.println(lineSeparator + "</ul>" + lineSeparator);
////                }
////                System.out.println(lineSeparator + "<li>");
//            } else {
////                System.out.println(lineSeparator + "<li>");
//            }
            this.level = newLevel;
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int position) {
            if (tag == HTML.Tag.H1 || tag == HTML.Tag.H2
                    || tag == HTML.Tag.H3 || tag == HTML.Tag.H4
                    || tag == HTML.Tag.H5 || tag == HTML.Tag.H6 || tag == HTML.Tag.PRE) {
                inHeader = false;
            }
//            if (level == 7) {
////                System.out.print(level + ": " + new String(text));
//            }
            // work around bug in the parser that fails to call flush
//            if (tag == HTML.Tag.HTML) {
//            }
        }

        Set<KendoUIDataItem> items = new HashSet<KendoUIDataItem>();
        String componentName = null;
        String attributeName = null;
        String explanation = null;
        String code = null;

        @Override
        public void handleText(char[] text, int position) {
            this.text = text;
            if (inHeader && level == 1) {
                componentName = new String(text);
            }
            if (inHeader && level == 3) {
                attributeName = new String(text);
            }
            if (level == 4) {
                explanation = "<h3>"+new String(text)+"</h3>";
            }
            if (level == 7) {
                code = escapeHTML(new String(text));
                KendoUIDataItem item = new KendoUIDataItem(attributeName, null, 
                        explanation+"<p>"+code, null);
                if (!items.contains(item)
                        && attributeName != null
                        && !attributeName.startsWith("(")
                        && !Character.isUpperCase(attributeName.charAt(0))) {
                    items.add(item);
                }
                System.out.print(level + ": " + new String(text));

//                System.out.print(level + ": " + new String(text));
            }
            result.put(componentName, items);
        }

        public static String escapeHTML(String s) {
            StringBuilder out = new StringBuilder(Math.max(16, s.length()));
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                    out.append("&#");
                    out.append((int) c);
                    out.append(';');
                } else {
                    out.append(c);
                }
            }
            String addBreak = out.toString().replace("\n", "<br />\n");;
            return addBreak;
        }

//
//        @Override
//        public void handleText(char[] data, int pos) {
//            if (startPos >= 0) {
//                startPos = pos;
//            }
//        }
//
//        //http://stackoverflow.com/questions/9580684/how-to-retrieve-title-of-a-html-with-the-help-of-htmleditorkit
//        
//        @Override
//        public void handleEndTag(HTML.Tag t, int pos) {
//            super.handleEndTag(t, pos);
//            String h1Content = null;
//            List<KendoUIDataItem> items = new ArrayList<KendoUIDataItem>();
//            if (t == HTML.Tag.H1) {
//                h1Content = html.substring(startPos, pos);
//                startPos = -1;
//            }
//            if (t == HTML.Tag.H3) {
////                String h3Content = html.substring(startPos, pos);
////                items.add(new KendoUIDataItem("hello", "b", "c", "d"));
//                startPos = -1;
//            }
//            result.put(h1Content, Arrays.asList(
//                    new KendoUIDataItem("hello1", "", "c", "d"),
//                    new KendoUIDataItem("hello2", "", "c", "d")
//            ));
//
////            result.put(h1Content, items);
//        }
//
//        @Override
//        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
//            super.handleStartTag(t, a, pos);
//            if (t == HTML.Tag.H1) {
//                startPos = pos;
//            }
//            if (t == HTML.Tag.H3) {
//                startPos = pos;
//            }
////            if (t.toString().equals("h3")) {
//////                java.lang.String value = (java.lang.String) a.getAttribute(HTML.Attribute.HREF);
////                result.put("kendoCalendar", Arrays.asList(
////                        new KendoUIDataItem(t.toString(), "b", "c", "d")));
////            }
//        }
    }

    public static class HTMLParse extends HTMLEditorKit {

        @Override
        public HTMLEditorKit.Parser getParser() {
            return super.getParser();
        }
    }

    private static final String TYPE = "type";   //NOI18N
    private static final String NAME = "name";   //NOI18N

    private enum Tag {

        object, property, doc, template, notinterested;
    }

    private String objectName;
    private String name;
    private String type;
    private String documentation;
    private String template;
    private List<KendoUIDataItem> items;

    private Tag inTag = Tag.notinterested;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals(Tag.object.name())) {
            objectName = attributes.getValue(NAME);
            items = new ArrayList<KendoUIDataItem>();
        } else if (qName.equals(Tag.property.name())) {
            name = attributes.getValue(NAME);
            type = attributes.getValue(TYPE);
            documentation = "";
            template = "";
        }
        try {
            inTag = Tag.valueOf(qName);
        } catch (IllegalArgumentException iae) {
            inTag = Tag.notinterested;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(Tag.object.name())) {
            result.put(objectName, items);
        } else if (qName.equals(Tag.property.name())) {
            items.add(new KendoUIDataItem(name, type, documentation, template));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (inTag) {
            case doc:
                documentation = documentation + new String(ch, start, length);
                break;
            case template:
                template = template + new String(ch, start, length);
                break;
            default:
        }
    }
}
