package com.insworks.plugin.utils

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.BaseVariantImpl
import com.insworks.plugin.model.Locl
import com.insworks.plugin.xml.XmlUtil
import groovy.json.JsonSlurper
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

import static com.insworks.plugin.utils.DirOption.findAllGradleFile
import static com.insworks.plugin.utils.DirOption.findAllLibsDir
import static com.insworks.plugin.utils.StreamUtil.writeFile

public class AutoCreateUtil {
    static def autoCreateDirAndFile(Project project, boolean isApp, String dirName, String jsonName, DomainObjectSet<BaseVariant> variants) {
        autoCreateGlobalDir(project, isApp)
        writeFile(project, "$project.rootDir/${dirName}/${jsonName}.json", isApp ? Locl.preModuleJsonContent : Locl.preLibJsonContent, false, true)
        //合并gradle文件
        findAllGradleFile(project, jsonName, dirName).each { f ->
            project.apply from: f.absoluteFile
        }

        //创建
        project.android.sourceSets.main {
            //解析json文件 并且创建code、res、assets、libs文件夹
            def kv = new JsonSlurper().parseText(project.file("$project.rootDir/$dirName/${jsonName}.json").text).func
            kv.keySet().each { k ->
                def hasDir = false
                project.file("$project.rootDir/$dirName/$k").listFiles().each { f ->
                    if (f.name.startsWith("code")) {
                        hasDir = true
                        java.srcDirs += f.absolutePath
                    } else if (f.name.startsWith("res")) {
                        hasDir = true
                        res.srcDirs += f.absolutePath
                    } else if (f.name.startsWith("assets")) {
                        hasDir = true
                        assets.srcDirs += f.absolutePath
                        res.srcDirs += f.absolutePath
                    }
                }
                if (!hasDir) {
                    createDir(project, dirName, k, kv.get(k))
                    java.srcDirs += "$project.rootDir/$dirName/$k/code"
                    res.srcDirs += "$project.rootDir/$dirName/$k/res"
                    assets.srcDirs += "$project.rootDir/$dirName/$k/assets"
                }

            }
            //合并清单文件
            String libManifestCacheFile = "$project.rootDir/cache/manifest/$dirName/AndroidManifest.xml"
            XmlUtil.mergeManifest(project, libManifestCacheFile, jsonName, dirName)
            // 引用合并后的清单文件
            res.srcDirs += project.file(libManifestCacheFile).parentFile.path
            manifest.srcFile libManifestCacheFile
        }


        //给子模块lib文件夹添加全局aar引用
        addProjectRepos(project, jsonName, dirName)
        project.dependencies {
            //引用所有子模块libs文件夹
            findAllLibsDir(project, jsonName, dirName).each { f ->
                api project.fileTree(dir: f.absoluteFile, include: ['*.jar'], exclude: [])
            }
        }
        autoRegisterActivityInMani(project, jsonName, dirName, variants)
    }
    /**
     * 自动给Activity注册清单文件
     * @param variants
     * @return
     */
    static def autoRegisterActivityInMani(Project project, jsonName, dirName, DomainObjectSet<BaseVariant> variants) {
        variants.all { variant ->
            variant = (BaseVariantImpl) variant
            HashSet<String> packSet= new HashSet<String>()
            variant.sourceSets.forEach {
                it.javaDirectories.forEach { f ->
                    File dir = new File(f.path)
                    if (dir.isDirectory()) {
                        dir.eachFileRecurse {
                            File file ->
                                if (file.name.endsWith("Activity.java") || file.name.endsWith("Activity.kt")) {
                                    //获取到所有的以Activity结尾的源代码文件
                                    String packageName = StreamUtil.getActivityPackageName(file)
                                    // 获取到需要注册的Activity的包名
                                    if (packageName != null) {
                                        //拼接全类名路径
                                        String quanClassName = packageName + "." + file.name.replace(".java", "").replace(".kt", "")
                                        packSet.add(quanClassName)

                                        println("====quanClassName=======" + quanClassName)
                                    }
                                }
                        }
                    }
                }
            }
            //注册到清单文件
            XmlUtil.registerActManifest(project, dirName, packSet)
            /*//这个方便一些 但只能获取Java源码 期待官方升级 增加kt的支持
            Class actClaz = Class.forName("android.app.Activity")
            variant.getSourceFolders(SourceKind.JAVA).forEach {
                it.files.forEach { f ->
                    println("====getSourceFolde" +
                            "rs=======" + f.absoluteFile)
                    Class clazz = f.absoluteFile.getClass()
                    //判断actClaz是否为clazz的父类
                    if (actClaz.isAssignableFrom(clazz)) {

                        System.out.println("A是 ${f.absoluteFile}的父类")
                    }
                }
            }*/


        }
    }
    /* static boolean isActivity(CtClass ctClass) {
         CtClass superClass = ctClass.getSuperclass()
         if (superClass == null) {
             return false
         }
         CtClass activityClass = superClass.getClassPool().get("android.app.Activity")
         CtClass appCompatActivityClass = superClass.getClassPool().get("android.support.v7.app.AppCompatActivity")
         CtClass fragmentActivityClass = superClass.getClassPool().get("android.support.v4.app.FragmentActivity")
         while (superClass != activityClass && superClass != fragmentActivityClass && superClass != appCompatActivityClass) {
             if (superClass.getPackageName().startsWith("java.")) {
                 return false
             }
             superClass = superClass.getSuperclass()
         }
         return true
     }*/
    /**
     * 创建文件夹
     * @param project
     * @param dirName
     * @param k
     * @param moduleName
     * @return
     */
    static def createDir(Project project, String dirName, String k, String moduleName) {
        println "自动生成${k}模块文件夹模板"
        def fileRoot = project.file("$project.rootDir/${dirName}/$k")

        //创建java code 文件夹
        def codeDir = "$fileRoot/code"
        project.file("${codeDir}/com/insworks/$k").mkdirs()

        //创建libs文件夹
        def libsDir = "$fileRoot/libs"
        project.file("${libsDir}").mkdirs()

        //创建assets文件夹
        def assetsDir = "$fileRoot/assets"
        project.file("${assetsDir}").mkdirs()

        //创建res文件夹
        def resRootDir = "$fileRoot/res"
        def resDir = new ArrayList<String>()
        resDir.add("$resRootDir/drawable")
        resDir.add("$resRootDir/mipmap-xhdpi")
        resDir.add("$resRootDir/mipmap-xxhdpi")
        resDir.add("$resRootDir/mipmap-xxxhdpi")
        resDir.add("$resRootDir/layout")
        resDir.add("$resRootDir/values")
        resDir.forEach({
            project.file(it).mkdirs()
        })
        //创建string
        def stringfile = "$resRootDir/values/string.xml"
        writeFile(project, stringfile, Locl.preStringsContent, false, false)

        //创建AndroidManifest.xml
        def manifest = "$resRootDir/AndroidManifest.xml"
        writeFile(project, manifest, Locl.preManifestsContent, false, false)

        //创建build.gradle
        def gradleFile = "$resRootDir/build.gradle"
        writeFile(project, gradleFile, Locl.preSubBuildContent, false, false)


        //创建文档文件
        def readme = "${fileRoot}/${moduleName}.md"
        writeFile(project, readme, "# ${moduleName}", false, false)
    }


    /**
     * 生成globa文件夹 并且开启全局引用
     * @param isApp
     */
    static def autoCreateGlobalDir(Project project, boolean isApp) {
        println("===开启全局global====")
        //引用全局libs
        project.getRootProject().allprojects {
            repositories {
                flatDir {
                    dirs "$project.rootDir/global/libs"
                }
            }
        }

        if (isApp) {

        } else {
            //全局dependencies文件由lib进行引用
            def globalDependPath = "$project.rootDir/global/dependencies.gradle"
            writeFile(project, globalDependPath, Locl.preGlobalBuildContent, false, true)
            project.apply from: globalDependPath

            //创建全局libs文件夹
            def libsDir = "$project.rootDir/global/libs"
            project.file("${libsDir}").mkdirs()


            project.dependencies {
                api project.fileTree(dir: "$project.rootDir/global/libs", include: ['*.jar'], exclude: [])
            }
        }
    }
    /**
     * 给子模块lib文件夹添加全局aar引用
     * @param dirName
     * @return
     */
    static def addProjectRepos = { Project project, String jsonName, String dirName ->
        def subLibsList = findAllLibsDir(project, jsonName, dirName)
        if (subLibsList.size() > 0) {
            //如果子模块没有libs文件夹 会爆You must specify at least one directory for a flat directory repository
            //flatDir里面必须得有内容 为了兼容没有libs文件夹的情况 做个size判断
            project.getRootProject().allprojects {
                repositories {
                    flatDir {
                        //aar文件需要flat指定
                        subLibsList.each { f ->
                            dirs f.absoluteFile
//                            println("引用${dirName}模块的libs ${f.absoluteFile}")
                        }
                    }
                }
            }
        }
    }
}


