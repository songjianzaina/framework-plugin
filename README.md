# framework-plugin 组件化架构插件

## 优势

* 轻量级组件框架
* 即插即用
* 使用json文件动态配置, 减少同步时间

## 使用

1. 在项目的 `build.gradle` 中添加：

```
buildscript {
  repositories {
   ...
	maven { url 'https://dl.bintray.com/songjianzaina/insoan' }
   }
  dependencies {
     classpath 'com.insworks.plugin:framework-plugin:1.0.5'
  }
}
```

2. 在宿主module中使用插件

```
apply plugin: 'framework-plugin'
```
3. 同步工程,等待文件夹自动生成

## 配置

你可以在build.gradle中配置插件的几个属性，如果不设置，所有的属性都使用默认值

```
frame{
    subDirName "androidModule"//子模块目录 App默认androidModule Lib默认androidLib
    jsonName "androidModule"//子模块json文件名 默认同上

}
```

## 更新历史
|   版本号       |    功能点   |链接 |
|----------|-------|-------|
| 1.0.0| 初步实现架构文件自动生成 |[ ![Download](https://api.bintray.com/packages/songjianzaina/insoan/framework-plugin/images/download.svg?version=1.0.0) ](https://bintray.com/songjianzaina/insoan/framework-plugin/1.0.0/link)|
| 1.0.1|  增加全局文件夹以及全局libs      |  [ ![Download](https://api.bintray.com/packages/songjianzaina/insoan/framework-plugin/images/download.svg?version=1.0.1) ](https://bintray.com/songjianzaina/insoan/framework-plugin/1.0.1/link)|
| 1.0.2|  增加子模块目录名和配置文件名自定义配置      | [ ![Download](https://api.bintray.com/packages/songjianzaina/insoan/framework-plugin/images/download.svg?version=1.0.2) ](https://bintray.com/songjianzaina/insoan/framework-plugin/1.0.2/link) |
|  1.0.3    |  优化清单文件合并     |  [ ![Download](https://api.bintray.com/packages/songjianzaina/insoan/framework-plugin/images/download.svg?version=1.0.3) ](https://bintray.com/songjianzaina/insoan/framework-plugin/1.0.3/link)|
|  1.0.4    |  增加Activity自动注册清单文件 (还未完善)    |  [ ![Download](https://api.bintray.com/packages/songjianzaina/insoan/framework-plugin/images/download.svg?version=1.0.4) ](https://bintray.com/songjianzaina/insoan/framework-plugin/1.0.4/link)|
|  1.0.5    |  1.升级gradle依赖至4.0.0  <br> 2.新增values目录下attr和styles文件的自动生成 <br> 3.解决子模块libs目录so库无法引用的问题 <br> 4.优化插件加载方式 提升构建速度 <br> 5.移除多余log    |  [ ![Download](https://api.bintray.com/packages/songjianzaina/insoan/framework-plugin/images/download.svg?version=1.0.5) ](https://bintray.com/songjianzaina/insoan/framework-plugin/1.0.5/link)|

