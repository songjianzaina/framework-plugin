package com.insworks.plugin.utils

import com.android.tools.r8.code.F
import org.apache.http.util.TextUtils
import org.dom4j.Attribute
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
import org.gradle.internal.impldep.org.apache.commons.lang.ObjectUtils

public class DomUtil {
    /**
     * 获取Document
     * @param file
     * @return
     */
    public static Document getDocument(String filePath) {
        return getDocument(new File(filePath))
    }
    /**
     * 获取Document
     * @param file
     * @return
     */
    public static Document getDocument(File file) {
        SAXReader saxReader = new SAXReader()
        return saxReader.read(file)
    }

    /**
     * 获取根节点
     * @param filePath
     * @return
     */
    public static Element getRootElement(String filePath) {
        return getRootElement(new File(filePath))
    }

    /**
     * 获取根节点
     * @param file
     * @return
     */
    public static Element getRootElement(File file) {
        return getDocument(file).getRootElement()
    }

    /**
     * 获取节点下制定属性值
     * @param element
     * @param attrName
     * @return
     */
    public static String getEleAttrValue(Element element, String attrName) {
        Iterator<Attribute> attributes = element.attributeIterator()
        while (attributes.hasNext()) {
            Attribute att = attributes.next()
            if (att.getName() == attrName && !TextUtils.isEmpty(att.value)) {
                return att.value
            }
        }
        return null
    }

    /**
     * 将某一xml中某一节点下的内容添加到指定节点下
     * @param parentElement 指定节点的父节点
     * @param nodeName 指定节点名称
     * @param target 目标节点
     */
    public static void getNodeContent(Element parentElement, String nodeName, Element target) {
        Iterator<Element> subList = parentElement.elementIterator()
        while (subList.hasNext()) {
            Element subEle = subList.next()
            if (subEle.getName() == nodeName) {
                Iterator<Element> ssubList = subEle.elementIterator()
                while (ssubList.hasNext()) {
                    Element ssubEle = ssubList.next()
                    target.add(ssubEle.detach())
                }
            }
        }
    }
    /**
     * 将某一xml中除了某一节点下之外的内容添加到到指定节点下
     * @param parentElement 指定节点的父节点
     * @param nodeName 指定节点名称
     * @param target 目标节点
     */
    public static void getExcludeNodeContent(Element parentElement, String nodeName, Element target) {
        Iterator<Element> subList = parentElement.elementIterator()
        while (subList.hasNext()) {
            Element subEle = subList.next()
            if (subEle.getName() != nodeName) {
                target.add(subEle.detach())
            }
        }
    }
}


