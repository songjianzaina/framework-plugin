package com.insworks.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.insworks.plugin.bean.FrameExtension
import com.insworks.plugin.utils.AutoCreateUtil
import org.apache.http.util.TextUtils
import org.dom4j.DocumentHelper
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * gradle生命周期 https://www.jianshu.com/p/2e19268bf387
 * gradle.settingsEvaluated->
 * gradle.projectsLoaded->
 * gradle.beforeProject->
 * project.beforeEvaluate->
 * gradle.afterProject-> //项目如果包含多个模块 那么该方法就会执行多次 监听所有项目
 * project.afterEvaluate->  //只监听当前项目
 * gradle.projectsEvaluated->
 * gradle.taskGraph.graphPopulated->
 * gradle.taskGraph.whenReady->
 * gradle.buildFinished
 */
class FrameworkPlugin implements Plugin<Project> {

    //模块根目录名称
    public String subModuleName
    //模块配置json名称
    public String moduleJsonName
    private Project mProject
    private def variants

    @Override
    void apply(Project project) {
        this.mProject = project
        println("=================================")
        println("====  组件架构 === Gradle插件  ====")
        println("=================================")
        //向project中添加frame节点
        project.extensions.add("frame", FrameExtension)

            //监听当前项目
            if (!project.android) {
                //未找到 android节点
                throw new IllegalStateException('Must apply \'com.android.application\' or \'com.android.library\' first!')
            } else {
                //存在android 节点
                TaskExecTimeInfo timeInfo = new TaskExecTimeInfo()
                //记录开始时间
                timeInfo.start = System.currentTimeMillis()
                AutoCreateUtil.autoCreateDirAndFile(project, initFrameConfig(project), subModuleName, moduleJsonName, variants)
                timeInfo.end = System.currentTimeMillis()
                //计算该 task 的执行时长
                println("组件架构插件总共花费时长${timeInfo.end -timeInfo.start}ms")
            }


    }
    /**
     * 获取frame节点下的配置信息
     * @return 返回是否为app
     */
    private boolean initFrameConfig(Project project) {
        FrameExtension ext = mProject.frame
        if (isApp(project)) {
            println("application开始配置")
            subModuleName = TextUtils.isEmpty(ext.subDirName) ? "androidModule" : ext.subDirName
            moduleJsonName = TextUtils.isEmpty(ext.jsonName) ? "androidModule" : ext.jsonName
            variants = (project.property("android") as AppExtension).applicationVariants
            return true
        } else {
            println("library开始配置")
            subModuleName = TextUtils.isEmpty(ext.subDirName) ? "androidLib" : ext.subDirName
            moduleJsonName = TextUtils.isEmpty(ext.jsonName) ? "androidLib" : ext.jsonName
            variants = (project.property("android") as LibraryExtension).libraryVariants
            return false
        }


    }

    /**
     * 检查当前环境 是app还是lib
     * @param project
     * @return
     */
    private static boolean isApp(Project project) {
        def isApp = project.plugins.withType(AppPlugin)
        def isLib = project.plugins.withType(LibraryPlugin)
        println("isApp : " + isApp)
        println("isLib : " + isLib)
        if (!isApp && !isLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }
        return isApp
    }

    //关于 task 的执行信息
    class TaskExecTimeInfo {

        long total = end - start    //task执行总时长

        String path
        long start      //task 执行开始时间
        long end        //task 结束时间

    }
}


