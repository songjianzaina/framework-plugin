package com.insworks.plugin.utils

import org.gradle.api.Project

public class StreamUtil {


    static def writeFile(Project project, String path, String content, boolean append, boolean isHoldContent) {

        try {
            project.file(path).parentFile.mkdirs()
            //文件不存在
            if (!project.file(path).exists()) {
                println("$path err message: \nFile does not exist")
                project.file(path).createNewFile()
                // 写入文件
                write(path, append, content)
            } else {
                //文件存在
                if (new FileReader(project.file(path)).readLines().size() > 0) {
                    //并且已经存在内容
                    if (isHoldContent) {
                        //保持原来内容不变
                    } else {
                        // 写入文件
                        write(path, append, content)
                    }
                } else {
                    println("============${path}不存在内容==========")
                    // 写入文件
                    write(path, append, content)
                }
            }

        } catch (IOException e) {
            e.printStackTrace()
        }

    }

    private static void write(String path, boolean append, String content) {
        def writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, append), "UTF-8"))
        writer.write(content)
        writer.close()
    }
    /**
     * 获取源码文件的包名
     * @param file
     * @return
     */
    public static String getActivityPackageName(File file) {
        String packageName = null
        boolean isNeededActivity = true
        BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))
        buffer.readLines().forEach { c ->
            if (c.startsWith("package")) {
                packageName = c.replace("package", "").replace(";","").trim()
            }
            if (c.contains("@不注册")) {
                isNeededActivity = false
            }

        }
        buffer.close()
        return isNeededActivity ? packageName : null
    }

}


