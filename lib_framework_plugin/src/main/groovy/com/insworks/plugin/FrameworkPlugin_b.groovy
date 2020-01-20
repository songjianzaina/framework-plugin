package com.insworks.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.apache.http.util.TextUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 该版本在java中有效 在kotlin中无效
 */
class FrameworkPlugin_b implements Plugin<Project> {

    //lib库根目录默认名称
    public String subLibName = "androidLib"
    //lib配置json默认名称
    public String libJsonName = "androidLib"
    //模块根目录默认名称
    public String subModuleName = "androidModule"
    //模块配置json默认名称
    public String moduleJsonName = "androidModule"
    private Project mProject
    //默认开启global
    private boolean mIsOpenGlobal = true

    @Override
    void apply(Project project) {
        this.mProject = project
        println("=================================")
        println("====  组件架构 === Gradle插件  ====")
        println("=================================")
        //向project中添加frame节点
        project.extensions.add("frame", FrameExtension)
        //节点加载完毕后
        project.afterEvaluate {

            if (!project.android) {
                //未找到 android节点
                throw new IllegalStateException('Must apply \'com.android.application\' or \'com.android.library\' first!')
            } else {
                //存在android 节点
                if (isApp(project)) {
                    //是 Application
                    println("检测到application")
                    initAppFrameConfig()
                    autoCreateDirAndFile(true, subModuleName, moduleJsonName)

                } else {
                    //是 library
                    println("检测到library")
                    initLibFrameConfig()
                    autoCreateDirAndFile(false, subLibName, libJsonName)
                }


            }
        }

    }
    /**
     * 获取frame节点下的配置信息
     * @return
     */
    private initAppFrameConfig() {
        FrameExtension ext = mProject.frame
        if (!TextUtils.isEmpty(ext.subDirName)) {
            subModuleName = ext.subDirName
        }
        if (!TextUtils.isEmpty(ext.jsonName)) {
            moduleJsonName = ext.jsonName
        }
    }
    /**
     * 获取frame节点下的配置信息
     * @return
     */
    private initLibFrameConfig() {
        FrameExtension ext = mProject.frame
        if (!TextUtils.isEmpty(ext.subDirName)) {
            subLibName = ext.subDirName
        }
        if (!TextUtils.isEmpty(ext.jsonName)) {
            libJsonName = ext.jsonName
        }

    }

    private autoCreateDirAndFile(boolean isApp, String dirName, String jsonName) {
        if (mIsOpenGlobal) {
            //生成global文件夹
            autoCreateGlobalDir(isApp)
        }
        println("===开启全局global3====$mProject.rootDir/${dirName}/${jsonName}.json")
        //创建json文件以及模块根目录
        writeFile("$mProject.rootDir/${dirName}/${jsonName}.json", isApp ? Locl.preModuleJsonContent : Locl.preLibJsonContent, false, true)
        //合并gradle文件
        DirOption.findAllGradleFile(mProject, jsonName, dirName).each { f ->
            mProject.apply from: f.absoluteFile
        }

        //合并清单文件
        //def bakPath = file("${buildDir}/bakApk123/") 后期可以考虑放在build文件夹中
        String libManifestCacheFile = "$mProject.rootDir/cache/manifest/$dirName/AndroidManifest.xml"
        mergeManifest(libManifestCacheFile, jsonName, dirName)

        //引用和创建
        mProject.android.sourceSets.main {
            // 引用合并后的清单文件
            res.srcDirs += mProject.file(libManifestCacheFile).parentFile.path
            manifest.srcFile libManifestCacheFile
            //解析json文件 并且创建code、res、assets、libs文件夹
            def kv = new groovy.json.JsonSlurper().parseText(mProject.file("$mProject.rootDir/$dirName/${jsonName}.json").text).func
            kv.keySet().each { k ->
                def hasDir = false
                mProject.file("$mProject.rootDir/$dirName/$k").listFiles().each { f ->
                    if (f.name.startsWith("code")) {
                        println("===引用子模块code====${f.absolutePath}")
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
                    createDir(mProject, dirName, k, kv.get(k))
                    java.srcDirs += "$mProject.rootDir/$dirName/$k/code"
                    res.srcDirs += "$mProject.rootDir/$dirName/$k/res"
                    assets.srcDirs += "$mProject.rootDir/$dirName/$k/assets"
                }

            }
        }
        //给子模块lib文件夹添加全局aar引用
        addProjectRepos(jsonName, dirName)
        mProject.dependencies {
            //引用所有子模块libs文件夹
            DirOption.findAllLibsDir(mProject, jsonName, dirName).each { f ->
                api mProject.fileTree(dir: f.absoluteFile, include: ['*.jar'], exclude: [])
            }
        }
    }
    /**
     * 生成globa文件夹 并且开启全局引用
     * @param isApp
     */
    private void autoCreateGlobalDir(boolean isApp) {
        println("===开启全局global====")
        //引用全局libs
        mProject.getRootProject().allprojects {
            repositories {
                flatDir {
                    dirs "$mProject.rootDir/global/libs"
                }
            }
        }

        if (isApp) {

        } else {
            //全局dependencies文件由lib进行引用
            def globalDependPath = "$mProject.rootDir/global/dependencies.gradle"
            writeFile(globalDependPath, Locl.preGlobalBuildContent, false, true)
            mProject.apply from: globalDependPath

            //创建全局libs文件夹
            def libsDir = "$mProject.rootDir/global/libs"
            mProject.file("${libsDir}").mkdirs()


            mProject.dependencies {
                api mProject.fileTree(dir: "$mProject.rootDir/global/libs", include: ['*.jar'], exclude: [])
            }
        }
    }
    /**
     * 检查当前环境 是app还是lib
     * @param project
     * @return
     */
    private boolean isApp(Project project) {
        def isApp = project.plugins.withType(AppPlugin)
        def isLib = project.plugins.withType(LibraryPlugin)
        println("isApp : " + isApp)
        println("isLib : " + isLib)
        if (!isApp && !isLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }
        return isApp
    }
    /**
     * 创建文件夹
     * @param project
     * @param dirName
     * @param k
     * @param moduleName
     * @return
     */
    def createDir(Project project, String dirName, String k, String moduleName) {
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
        writeFile(stringfile, Locl.preStringsContent, false)

        //创建AndroidManifest.xml
        def manifest = "$resRootDir/AndroidManifest.xml"
        writeFile(manifest, Locl.preManifestsContent, false)

        //创建build.gradle
        def gradleFile = "$resRootDir/build.gradle"
        writeFile(gradleFile, Locl.preSubBuildContent, false)


        //创建文档文件
        def readme = "${fileRoot}/${moduleName}.md"
        writeFile(readme, "# ${moduleName}", false)
    }

    def writeFile(String path, String content, boolean append) {
        try {
            mProject.file(path).parentFile.mkdirs()
            //文件不存在
            if (!mProject.file(path).exists()) {
                println("$path err message: \nFile does not exist")
                mProject.file(path).createNewFile()
            }
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件  使用BufferedWriter 解决编码问题
            def writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, append), "UTF-8"))
//            def writer = new FileWriter(path, append)
            writer.write(content)
            writer.close()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    def writeFile(String path, String content, boolean append, boolean islossless) {
        if (!islossless) {
            writeFile(path, content, append)
        } else {

            try {
                boolean hasContent = false
                mProject.file(path).parentFile.mkdirs()
                //文件不存在
                if (!mProject.file(path).exists()) {
                    println("$path err message: \nFile does not exist")
                    mProject.file(path).createNewFile()
                } else {
                    //文件存在
                    if (new FileReader(mProject.file(path)).readLines().size() > 0) {
                        //并且已经存在内容
                        println("============${path}已经存在内容==========")
                        hasContent = true
                    } else {
                        println("============${path}不存在内容==========")

                    }
                }
                if (hasContent && !append) {
                    //如果已经存在内容 同时不是追加情况  则不允许清除数据
                } else {
                    // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                    def writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, append), "UTF-8"))
                    writer.write(content)
                    writer.close()
                }

            } catch (IOException e) {
                e.printStackTrace()
            }
        }

    }
    /**
     * 合并清单文件
     * @param cacheManifest
     ile
     * @param dir
     ame
     * @return
     */
    def mergeManifest(String cacheManifestFile, String jsonName, String dirName) {
        String defManifestText = mProject.file("$mProject.rootDir/${mProject.getName()}/src/main/AndroidManifest.xml").text

        println("=============开始合并AndroidManifest.xml=================")
        int endIndex = defManifestText.indexOf("</application>")
        writeFile(cacheManifestFile, defManifestText.substring(0, endIndex), false)
        DirOption.getAllManifestXml(mProject, jsonName, dirName).each { f ->
            f.eachLine { line ->
                if (!line.contains("manifest") && !line.contains("<application") && !line.contains("application>") && !line.contains("encoding"))
                    writeFile(cacheManifestFile, "$line\n", true)
            }
        }
        writeFile(cacheManifestFile, defManifestText.substring(endIndex), true)
        println("==============结束合并AndroidManifest.xml================")

    }

    /**
     * 给子模块lib文件夹添加全局aar引用
     * @param dirName
     * @return
     */
    def addProjectRepos(String jsonName, String dirName) {
        def subLibsList = DirOption.findAllLibsDir(mProject, jsonName, dirName)
        if (subLibsList.size() > 0) {
            //如果子模块没有libs文件夹 会爆You must specify at least one directory for a flat directory repository
            //flatDir里面必须得有内容 为了兼容没有libs文件夹的情况 做个size判断
            mProject.getRootProject().allprojects {
                repositories {
                    flatDir {
                        //aar文件需要flat指定
                        DirOption.findAllLibsDir(mProject, jsonName, dirName).each { f ->
                            dirs f.absoluteFile
                            println("引用${dirName}模块的libs ${f.absoluteFile}")
                        }
                    }
                }
            }
        }
    }

}


