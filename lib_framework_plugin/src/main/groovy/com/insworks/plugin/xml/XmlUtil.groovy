package com.insworks.plugin.xml

import com.android.tools.r8.code.S
import com.insworks.plugin.utils.DirOption
import com.insworks.plugin.utils.DomUtil
import org.dom4j.Document
import org.dom4j.Element
import org.gradle.api.Project

import static com.insworks.plugin.utils.DirOption.getAllManifestXml
import static com.insworks.plugin.utils.StreamUtil.writeFile

public class XmlUtil {

    /**
     * 合并清单文件
     * @param cacheManifest
     ile
     * @param dir
     ame
     * @return
     */
    static def mergeManifest(Project project, String cacheManifestFile, String jsonName, String dirName) {
        println("=============开始合并AndroidManifest.xml=================")
        def baseManifile = "$project.rootDir/${project.getName()}/src/main/AndroidManifest.xml"
        writeFile(project, cacheManifestFile, mergeManifest(baseManifile, getAllManifestXml(project, jsonName, dirName)), false, false)
        println("==============结束合并AndroidManifest.xml================")
    }


    /**
     * 合并清单文件  合并的方式是 以宿主工程的清单文件为基础 将其他的清单文件内容追加进去
     * @param baseManifestFile 基础文件
     * @param targetManifestFile 输入文件
     * @param subManifestList 需要合并的文件集
     */
    static def mergeManifest(String baseManifestFile, List<File> subManifestList) {
        String hasApplication = false
        //获取宿主中的清单文件
        Document mian = DomUtil.getDocument(baseManifestFile)
        //获得宿主中第一个xml的根节点
        Element mianRootElement = (Element) mian.getRootElement()
        //有子模块清单文件根节点集
        List<Element> subRootEleList = getSubManifestRootEleList(subManifestList)
        mianRootElement.elementIterator().each { baseElement ->
            //先从宿主application节点下 开始合并
            if (baseElement.getName() == "application") {
                //宿主中 application节点
                hasApplication = true
                subRootEleList.each { element ->
                    //获取根节点下的package属性
                    String packageName = DomUtil.getEleAttrValue(element, "package")
                    //获取子模块中的清单文件
                    Iterator<Element> subList = element.elementIterator()
                    subList.each { subEle ->
                        //第一步 合并application节点下的所有数据 包括包名的拼接
                        mergeApplication(baseElement, subEle, packageName)
                        //第二步 合并application节点以外的数据 比如权限等等
                        mergeExcludeApplication(baseElement, subEle,)
                    }
                }

            } else {
                //宿主 清单文件 application节点外的处理  暂无
            }
        }
        if (!hasApplication) {
            // 宿主中没有application节点
            println(" err message: \n宿主中必须要定义application节点")
        }

        return mian.asXML()

    }

    /**
     * 获取所有被激活子模块的清单文件根节点
     * @return
     */
    static def getSubManifestRootEleList(List<File> subManifestList) {
        ArrayList<Element> list = new ArrayList<Element>()
        subManifestList.each { file ->
            //获得子模块中根节点下的节点信息
            list.add(DomUtil.getRootElement(file))
        }
        return list
    }
    /**
     * 合并application内节点数据
     * @param parent
     * @param subEle
     * @return
     */
    static def mergeApplication(Element parent, Element subEle, String packageName) {
        if (subEle.getName() == "application") {
            //子模块中 application节点
            Iterator<Element> ssubList = subEle.elementIterator()
            ssubList.each { ssubEle ->
                //activity android:name添加包名路径前缀
                if (packageName != null && ssubEle.getName() == "activity") {
                    String androidNameValue = DomUtil.getEleAttrValue(ssubEle, "name")
                    if (androidNameValue != null && androidNameValue.startsWith(".")) {
                        ssubEle.attribute("name").setValue(packageName + androidNameValue)
                    }
                }

                //application 节点下的所有节点 添加到宿主
                parent.add(ssubEle.detach())
            }
        }
    }
    /**
     * 合并 application外层数据
     * @param parent
     * @param subEle
     * @return
     */
    static def mergeExcludeApplication(Element parent, Element subEle) {
        if (subEle.getName() != "application") {
            //子模块中 权限啥的外层内容 加入到根节点下
            parent.add(subEle.detach())
        }
    }


    /**
     * 注册清单文件
     * @param cacheManifest
     ile
     * @param dir
     ame
     * @return
     */
    static def registerActManifest(Project project, String dirName, HashSet<String> packList) {
        println("=============开始注册Acitivity=================")
        String cacheManifestFile = "$project.rootDir/cache/manifest/$dirName/AndroidManifest.xml"
        String baseManifile = "$project.rootDir/${project.getName()}/src/main/AndroidManifest.xml"
        Document baseDoc = DomUtil.getDocument(baseManifile)
        Element appEle = baseDoc.getRootElement().element("application")
        //先判断缓存文件中是否存在 如果不存在就添加进去
        List<Element> list = DomUtil.getRootElement(cacheManifestFile).element("application").elements("activity")

        packList.forEach { p ->
            boolean isExist = false
            list.forEach { ele ->
                if (p.contains(ele.attribute("name").value)) {
                    //已经存在 不用添加
                    isExist = true
                }
            }
           if( !isExist){
               //不存在 添加
               appEle.addElement("activity")
                       .addAttribute("android:name", p)
           }
        }

        //重写清单文件
        writeFile(project, baseManifile, baseDoc.asXML(), false, false)
        println("==============结束注册Acitivity================")
    }
}


