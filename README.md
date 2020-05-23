## Intelligent Analyze Platform

应用层服务端项目, 整体采用Spring框架，SpringMVC实现restful接口规范实现。项目
项目分为account(账号系统)、follow(关注系统)、search(检索系统)、proxy(代理系统)、configure(行内数据配置系统)四个模块。

## Requirement

Java8 +, Maven3

## Prepare

First, 你需要在middleware根目录下执行下列命令,整个系统所需依赖将会被安装

    mvn clean install -DskipTests [-Pdev/test/online]

## Run

到相应的子系统下,如iap-account,执行mvn tomcat:run,或者在IntellIJ 右侧的maven projects中选择iap :: account -> plugins -> tomcat7 -> 右键tomcat7:run选择debug或run

## Configuration