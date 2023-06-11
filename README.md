![image-20230611131112677](https://chenlvtang.top/pics/20230611131136.png)

并没有拿到毕设中的高分，哈哈哈……（懂的都懂，就当作对自己所学知识的应用了，欢迎大家Fork参考学习和PR完善。

# TinyRASP

My graduation project, from open source, gives back to open source

### 背景

​	传统的Web应用防护技术，如WAF（Web Application Firewall），因为是依据已定的则进行拦截，因此常常不能应对各种经过编码或是变形的攻击，同时规则的好坏对漏报、误报率影响较大，且无法对未披露利用方式的漏洞进行防御。

​	因此，本文基于新兴的RASP技术，设计并实现了一个针对Java的漏洞防护系统。通过在漏洞利用的关键函数上埋下RASP探针，实现运行时实时检测和日志记录，在能**检测到漏洞并实现告警页面重定向的同时，借助ELK（Elasticsearch、Logstash、Kibana）架构来对攻击者信息进行可视化管理以便溯源分析**。最后，实验结果表明，本文设计的系统具有良好的拦截效果和兼容性，有一定的应用场景。

**关键词**：RASP；漏洞防护；Java Web；检测和拦截；ELK；日志可视化

### 系统架构

#### 1. 功能模块图

![rasp系统功能模块](https://chenlvtang.top/pics/20230611131755.png)

#### 2. Agent架构图

![rasp架构](https://chenlvtang.top/pics/20230611131919.png)

#### 3. Agent项目结构

![rasp-Agent文件结构](https://chenlvtang.top/pics/20230611133346.png)

#### 4. 运行流程

![rasp运行流程](https://chenlvtang.top/pics/20230611133511.png)

#### 5. 测试靶场拓扑图

![rasp测试部署网络架构](https://chenlvtang.top/pics/20230611135427.png)

### 成果及展望

✔漏洞防护：

+ RCE
+ 任意文件读取
+ SQL注入
+ 反序列化
+ JNDI注入
+ SpEL表达式注入

✔兼容性：

+ Spring Boot、Tomcat
+ Java17、Java8
+ 高低版本Servlet（高版本中包名为Jakarta而不是Javax）

✔攻击日志可视化管理：

+ 总日志浏览
+ 攻击信息可视化

❌防护规则还需完善（ORZ，目前非常简陋，后期赶时间，大多用的JRASP的规则，哈哈哈，Sorry

❌漏洞的覆盖面较低，目前完成六类漏洞

❌缺少一个用户配置读取模块，只能硬编码规则

❌做着做着发现，Byte Buddy其实好像比Javassit更好用，哈哈 ，但是不想重构

### 部署效果

<video src="https://chenlvtang.top/rasp.mkv"></video>

漏洞防护（部署前和部署后的对比图）：

![image-20230611134734197](https://chenlvtang.top/pics/20230611134917.png)

兼容性测试：

![image-20230611134912645](https://chenlvtang.top/pics/20230611134920.png)

日志可视化：

![image-20230611135144779](https://chenlvtang.top/pics/20230611135147.png)

### 致谢

感谢各位师傅的技术博客、开源项目的慷慨大方，感谢ChatGPT，JRASP、OpenRASP，感谢感谢(●ˇ∀ˇ●)

### 参考

http://www.jrasp.com/

https://github.com/jvm-rasp/jrasp-agent

https://github.com/baidu/openrasp

https://www.cnblogs.com/bitterz/p/15152287.html

https://www.cnblogs.com/Zzang/p/14845578.html

https://javasec.org/java-rasp/Hook.html



