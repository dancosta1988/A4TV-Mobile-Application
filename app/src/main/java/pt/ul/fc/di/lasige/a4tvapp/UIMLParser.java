package pt.ul.fc.di.lasige.a4tvapp;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class UIMLParser {

    private Document doc;

    public UIMLParser(){
    }

    public void setUIML(String uiml){

        try {
            //File inputFile = new File("input.txt");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(uiml));
            doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDocumentNull(){
        return doc == null;
    }

    public String getRootName(){
       return doc.getDocumentElement().getNodeName();
    }

    public NodeList getElementsByTagName(String tag) {
        NodeList nList = doc.getElementsByTagName(tag);
        return nList;
    }

    //get the description of an element given the id
    public String getDescription(String id){
        //System.err.println("Checking text from id "+ id);
        //get the text of the element
        String text = getPropertyValueFromPartName(id, "text");
        if(text != "" && text.charAt(text.length()-1) != '.')
            text += ". ";

        //if empty try to get description from a label
        if(text == "") {
            text = getPropertyValueFromPartName(id, "label");
            //if label empty try to get a description from a child of this node

        }


            ArrayList<String> childs = getChildPartsWithId(id);
            // System.err.println(" Didnt find! Has childs? " + childs.size());
            for (String child : childs) {
                //text = getPropertyValueFromPartName(child, "text");//child description
                //recursion
                //System.err.println(" Didnt find lets see the child  "+ child);
                String desChild = getDescription(child);
                if(!text.contains(desChild))
                    text += " " + desChild;
                if(text != "" && text.charAt(text.length()-1) != '.')
                    text += ". ";
                    /*if (text == "") {
                        text = getPropertyValueFromPartName(child, "label"); //child label
                    }
                    if (text != "") {
                        break;
                    }*/
            }


        return text.trim();
    }

    public ArrayList getPartsWithClass(String class_name){

        ArrayList result = new ArrayList();
        NodeList parts = this.getElementsByTagName("part");

        for (int temp = 0; temp < parts.getLength(); temp++) {
            Node nNode = parts.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String partClass = eElement.getAttribute("class");
                String partId = eElement.getAttribute("id");
                //if not All get only the elems with received class
                if (!class_name.equalsIgnoreCase("All") && partClass.equalsIgnoreCase(class_name)) {
                    result.add(partId);
                    //else get all elems
                }else if(class_name.equalsIgnoreCase("All") && !partId.equalsIgnoreCase("a4tv_app")){
                    result.add(partId);
                }
            }
        }
        return result;
    }

    public ArrayList getChildPartsWithId(String id){

        ArrayList result = new ArrayList();
        NodeList parts = this.getElementsByTagName("part");

        for (int temp = 0; temp < parts.getLength(); temp++) {
            Node nNode = parts.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String partId = eElement.getAttribute("id");
                if (partId.equalsIgnoreCase(id)) {
                    NodeList childs = nNode.getChildNodes();
                    for (int i = 0; i < childs.getLength(); i++) {
                        Element cElement = (Element) childs.item(i);
                        result.add(cElement.getAttribute("id"));
                    }
                    break;
                }
            }
        }
        return result;
    }


    public String getPropertyValueFromPartName (String part_name, String prop_name) {
        NodeList parts = this.getElementsByTagName("property");

        for (int temp = 0; temp < parts.getLength(); temp++) {
            Node nNode = parts.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String partName = eElement.getAttribute("part-name");
                String propName = eElement.getAttribute("name");
                if (partName.equalsIgnoreCase(part_name) && propName.equalsIgnoreCase(prop_name)) {
                    return eElement.getTextContent();
                }
            }
        }
        return "";
    }
}