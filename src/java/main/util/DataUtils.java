package main.util;


import javafx.scene.shape.Rectangle;
import main.model.FileMetadata;
import main.model.KeyMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DataUtils {

    public DataUtils() {
    }

    public static String readFile(String path) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded, Charset.defaultCharset());
    }

    public static byte[] readBitsFormFile(String filename) {
        File file = new File(filename);
        byte[] data = new byte[(int)file.length()];
        try {
            FileInputStream inputStream = new FileInputStream(new File(filename));
            while ((inputStream.read(data)) != -1) {
            }
            inputStream.close();
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] readKeyBits(String filename, int offset, int length) {
        byte[] data = new byte[length];
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            inputStream.skip(offset);
            inputStream.read(data);
            inputStream.close();
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String hash(byte[] message){
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message);
            StringBuilder sb = new StringBuilder(2*hash.length);
            for(byte b : hash){
                sb.append(String.format("%02x", b&0xff));
            }

            digest = sb.toString();

        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return digest;
    }

    public static FileMetadata readFileMetadata(byte[] data){
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setSize(byteToLong(data, 0, 8));
        fileMetadata.setMetaSize(byteToLong(data, 8, 16));
        fileMetadata.setHash(byteToString(data,16, 48));
        fileMetadata.setFileName(byteToString(data, 48, (int) (fileMetadata.getMetaSize() - 8)));
        return fileMetadata;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long byteToLong(byte[] data, int start, int end){
        byte[] bytes = new byte[end -start];
        int j = 0;
        for(int i  = start; i < end; i++){
            bytes[j++] = data[i];
        }
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static String byteToString(byte[] data, int start, int end){
        byte[] hash = new byte[end - start];
        int j = 0;
        for(int i  = start; i < end; i++){
            hash[j++] = data[i];
        }
        return new String(hash);
    }

    public static KeyMetadata readKeyMetadata(String path){
        KeyMetadata keyMetadata = new KeyMetadata();
        long id, size, meta_size, offset;
        String[] reads = new String[4];
        int i = 0;

        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            while ((strLine = br.readLine()) != null)   {
               reads[i++] = strLine;
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        id = Long.parseLong(reads[0]);
        size = Long.parseLong(reads[1]);
        meta_size = Long.parseLong(reads[2]);
        offset = Long.parseLong(reads[3]);

        keyMetadata.setId(id);
        keyMetadata.setSize(size);
        keyMetadata.setMeta_size(meta_size);
        keyMetadata.setOffset(offset);

        return keyMetadata;
    }

    public static void writeKeyMetadata(String path, KeyMetadata keyMetadata){
        File fout = new File(path);
        FileOutputStream fos;

        try {
            fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));


            bw.write(((Long) keyMetadata.getId()).toString());
            bw.newLine();
            bw.write(((Long) keyMetadata.getSize()).toString());
            bw.newLine();
            bw.write(((Long) keyMetadata.getMeta_size()).toString());
            bw.newLine();
            bw.write(((Long) keyMetadata.getOffset()).toString());

            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateKey(String key, String bs, String count){
        String command = "dd if=/dev/urandom of=" + key + " bs=" + bs + " count=" + count + " iflag=fullblock";
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void writeDecList(ArrayList<Rectangle> list) {

        try {

            DocumentBuilderFactory dFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder build = dFact.newDocumentBuilder();
            Document doc = build.newDocument();

            Element root = doc.createElement("rectangles");
            doc.appendChild(root);

            for (Rectangle dtl : list) {
                Element rectangle = doc.createElement("rectangle");
                root.appendChild(rectangle);

                Element x = doc.createElement("x");
                x.appendChild(doc.createTextNode(String.valueOf(dtl.getX())));
                rectangle.appendChild(x);

                Element y = doc.createElement("y");
                y.appendChild(doc.createTextNode(String.valueOf(dtl.getY())));
                rectangle.appendChild(y);

                Element width = doc.createElement("width");
                width.appendChild(doc.createTextNode(String.valueOf(dtl.getWidth())));
                rectangle.appendChild(width);

                Element height = doc.createElement("height");
                height.appendChild(doc.createTextNode(String.valueOf(dtl.getHeight())));
                rectangle.appendChild(height);
            }

            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer aTransformer = tranFactory.newTransformer();

            aTransformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");

            aTransformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "4");
            aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            try {

                FileWriter fos = new FileWriter("/home/mihter/Desktop/.data.xml");
                StreamResult result = new StreamResult(fos);
                aTransformer.transform(source, result);

            } catch (IOException e) {

                e.printStackTrace();
            }

        } catch (TransformerException ex) {
            System.out.println("Error outputting document");

        } catch (ParserConfigurationException ex) {
            System.out.println("Error building document");
        }
    }

    public static ArrayList<Rectangle> readDecList(){
        File xmlFile = new File("/home/mihter/Desktop/.data.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("rectangle");

            ArrayList<Rectangle> rectangles = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                rectangles.add(getRectangle(nodeList.item(i)));
            }
            return rectangles;

        } catch (SAXException | ParserConfigurationException | IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }


    private static Rectangle getRectangle(org.w3c.dom.Node node) {
        Rectangle rectangle = new Rectangle();
        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            Element element = (Element) node;
            rectangle.setX(Double.parseDouble(getTagValue("x", element)));
            rectangle.setY(Double.parseDouble(getTagValue("y", element)));
            rectangle.setWidth(Double.parseDouble(getTagValue("width", element)));
            rectangle.setHeight(Double.parseDouble(getTagValue("height", element)));
        }
        return rectangle;
    }


    private static String getTagValue(String tag, Element element) {
        org.w3c.dom.NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        org.w3c.dom.Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }

}