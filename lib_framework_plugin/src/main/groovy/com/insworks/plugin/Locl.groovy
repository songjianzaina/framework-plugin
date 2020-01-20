package com.insworks.plugin


import groovy.json.JsonSlurper
import org.gradle.api.Project

/**
 * 预装数据
 */
class Locl {

  static String preLibJsonContent="{\n" +
          "  \"desc\": \"Lib注册清单, 可自行增删\",\n" +
          "  \"func\": {\n" +
          "    \"base\": \"顶层Base库\",\n" +
          "    \"net\": \"网络库\",\n" +
          "    \"data\": \"数据库\",\n" +
          "    \"log\": \"日志打印库\"\n" +
          "  }\n" +
          "}"
  static String preModuleJsonContent="{\n" +
          "  \"desc\": \"功能模块注册清单,可自行增删\",\n" +
          "  \"func\": {\n" +
          "    \"splash\": \"应用启动模块\",\n" +
          "    \"login\": \"登录注册模块\",\n" +
          "    \"mian\": \"主框架模块\",\n" +
          "  }\n" +
          "}"
  static String preGlobalBuildContent="//全局依赖\n" +
          "dependencies {\n" +
          "    \n" +
          "}"
  static String preSubBuildContent="//子模块依赖\n" +
          "dependencies {\n" +
          "    \n" +
          "}"

  static String preStringsContent="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
          "<resources>\n" +
          "\n" +
          "</resources>"

  static String preManifestsContent="<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
          "\n" +
          "</manifest>"

}


