package dna.origins.util.toolbar;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlConversion {
    private static final Logger logger = LoggerFactory.getLogger(XmlConversion.class);

    /**
     * xml转换，暂时没有处理同名节点的情况
     *
     * @param xml
     * @return
     */
    public static Map<String, String> parseToMap(String xml) {
        if (StringUtils.isNotBlank(xml)) {
            try {
                Document document = DocumentHelper.parseText(xml);
                Element root = document.getRootElement(); // 获取根节点
                Map<String, String> map = parseToMap(root);
                return map;
            } catch (Exception e) {
                logger.error("can not parse Xml[{}] to map,cause:", xml, e);
            }
        }
        return null;
    }

    public static Map<String, String> parseToMap(Element root) {
        Map<String, String> map = new HashMap<>();
        List<Element> elementList = root.elements();
        //判断有没有子元素列表
        if (elementList.size() == 0) {
            map.put(root.getName(), root.getText());
        } else {
            //遍历
            for (Element e : elementList) {
                map.putAll(parseToMap(e));
            }
        }
        return map;
    }

    public static String parseToXmlString(JSONObject jsonObject) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        for (String key : jsonObject.keySet()) {
            String value = "<![CDATA[" + (jsonObject.getString(key) == null ? "" : jsonObject.getString(key)) + "]]>";
            sb.append("<" + key + ">" + value + "</" + key + ">");
        }
        sb.append("</xml>");
        return sb.toString();
    }
}
