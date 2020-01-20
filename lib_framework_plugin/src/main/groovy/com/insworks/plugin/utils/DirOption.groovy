package com.insworks.plugin.utils


import groovy.json.JsonSlurper
import org.gradle.api.Project


class DirOption {

    /**
     * 获取所有libs文件夹
     */
    static List<File> findAllLibsDir(Project project,jsonName, String dirName) {
        return getSubDirList(project, jsonName,dirName, "libs")
    }
    /**
     * 获取指定模块目录下所有被激活的 AndroidManifest.xml
     */
    static List<File> getAllManifestXml(Project project, String jsonName, String dirName) {

        return getSubFileList(project,jsonName, dirName, "AndroidManifest.xml")
    }


    /**
     * 获取所有已激活的 gradle 文件
     */
    static List<File> findAllGradleFile(Project project, String jsonName, String dirName) {
        return getSubFileList(project,jsonName, dirName, "build.gradle")
    }
    /**
     * 获取指定模块目录下的文件夹
     * @param project
     * @param dirName
     * @param subDir
     * @return
     */
    private static ArrayList getSubDirList(Project project, String jsonName,String dirName, subDir) {
        List<File> subDirList = new ArrayList()
        def json = new JsonSlurper().parseText(project.file("$project.rootDir/${dirName}/${jsonName}.json").text)
        json.func.keySet().each { k ->
            project.file("$project.rootDir/${dirName}/$k").listFiles().each { d ->
                if (d.name == subDir) {
                    subDirList.add(d)
                }
            }
        }
        return subDirList
    }
    /**
     * 获取指定模块目录下的文件
     * @param project
     * @param dirName
     * @param subFileName
     * @return
     */
    private static ArrayList getSubFileList(Project project, String jsonName, String dirName, subFileName) {
        List<File> subFileList = new ArrayList()
        def json = new JsonSlurper().parseText(project.file("$project.rootDir/${dirName}/${jsonName}.json").text)
        json.func.keySet().each { k ->
            project.file("$project.rootDir/${dirName}/$k").listFiles().each { d ->
                d.listFiles().each { f ->
                    if (f.name == subFileName) {
                        subFileList.add(f)
                    }
                }

            }
        }
        return subFileList
    }


}


