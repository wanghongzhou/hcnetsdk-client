package com.github.whz.hcnetsdk.util;

import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class InnerUtils {

    /**
     * 海康时间轴转化为时间轴
     */
    public static long hikAbsTimeToTimestamp(int absTime) {
        Calendar result = Calendar.getInstance(TimeZone.getDefault());
        result.set(Calendar.YEAR, (((absTime) >> 26) + 2000));
        result.set(Calendar.MONTH, ((absTime >> 22) & 15) - 1);
        result.set(Calendar.DAY_OF_MONTH, (absTime >> 17) & 31);
        result.set(Calendar.HOUR_OF_DAY, (absTime >> 12) & 31);
        result.set(Calendar.MINUTE, (absTime >> 6) & 63);
        result.set(Calendar.SECOND, (absTime) & 63);
        return result.getTimeInMillis();
    }

    /**
     * 创建文件所在目录.
     */
    public static boolean makeParentDirExists(File file) {
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            return parentDir.mkdirs();
        }
        return true;
    }

    /**
     * 写入文件.
     */
    public static void writeFile(byte[] bytes, String path) {
        File file = new File(path);
        InnerUtils.makeParentDirExists(file);
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(bytes);
            fos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 时间格式化
     */
    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 转换xml为map
     */
    @SneakyThrows
    public static Map<String, String> xmlToFlatMap(String xml, String rootElement) {
        Map<String, String> map = new HashMap<>();
        Document doc = parseXmlString(xml);

        NodeList rootNode = doc.getElementsByTagName(rootElement);
        if (Objects.isNull(rootNode) || rootNode.getLength() == 0) {
            return map;
        }

        Node root = rootNode.item(0);
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            map.put(node.getNodeName(), node.getTextContent());
        }
        return map;
    }

    private static Document parseXmlString(String xmlStr) throws ParserConfigurationException, IOException, SAXException {
        InputSource is = new InputSource(new StringReader(xmlStr));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }
}
