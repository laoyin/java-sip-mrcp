
# Java SIP MRCP
如果您有mrcp-server需求，可以在此申请[https://lowbibi.com/mrcp-server-ban-ben-fa-bu/]
基于mrcp-server的智能问答，b站演示视频：[https://www.bilibili.com/video/BV1Ea4y197Kc/]

你可以线上体验：[https://lowbibi.com/yinphone/#/call/call]
账号 zx1234      zx1234
分机 1001  密码 yin-123
拨号 1234



Java SIP MRCP 是一个基于 Java 的项目，实现了 SIP 协议和 MRCPv2 协议，用于实时语音转写（ASR）的媒体传输。


你可以基于此实现支持语音转写的客户端，
你也可基于此实现 java 版本 mrcp-server

![](audio/mrcp.gif)


mrcp协议介绍[https://lowbibi.com/mrcp/]

## 功能特点

- 支持 SIP 协议：实现了 SIP 协议的功能，包括呼叫建立、发送和接收 SIP 消息等。
- 支持 MRCPv2 协议：实现了 MRCPv2 协议，用于与语音识别引擎进行通信和控制。
- 媒体传输：通过 SIP 和 MRCPv2 协议，支持实时语音传输，将音频数据发送给语音识别引擎进行实时语音转写。
- 实时语音转写：与语音识别引擎进行集成，将实时的语音数据转写为文本。

目前已经测试对接的mrcp-server：

百度、腾讯等均ok。

## 安装和配置

### 依赖项

- Java 8 或更高版本
- 相关依赖库和框架（列出所需的任何外部库和其版本号）

### 环境要求

确保满足以下环境要求：

- 操作系统：支持的操作系统列表（例如Windows，Linux）
- Java运行时环境：Java 8 或更高版本

### 安装步骤

1. 下载或克隆 Java SIP MRCP 项目代码仓库。

2. 解决依赖项：根据项目的依赖配置文件（如 Maven 或 Gradle）下载和解决所需的依赖项。

3. 配置 SIP 参数：编辑配置文件，设置 SIP 服务器地址、端口号、用户凭证等相关参数。

4. 配置 MRCP 参数：配置 MRCP 服务器地址、端口号、语音识别引擎的相关信息。

5. 运行应用程序：执行启动命令或运行主类来启动 Java SIP MRCP 服务。

## 使用示例

提供一个简单的示例代码或指导步骤，以帮助用户了解如何使用 Java SIP MRCP 服务。

```java


```$xslt


https://github.com/laoyin/java-sip-mrcp/blob/master/src/main/java/com/mrcp/yxp/protocol/MrcpJavaClient.java

src/main/java/com/mrcp/yxp/protocol/MrcpJavaClient.java

示例就是MrcpJavaClient

初始化 sipagent。

userAgent = new UserAgent(this,
                new CustomConfig(LocalHostIp, new PipedOutputStream(), new ArrayBlockingQueue(100)),
                null, null);



try{
 sipRequest = userAgent.invite("sip:mrcp@"+MrcpServerIp+":"+MrcpServerPort, null);
} catch (SipUriSyntaxException e) {
    e.printStackTrace();
}


 //FileInputStream fileInputStream = new FileInputStream(audioPathLeft);
// 查看 sip-mrcp 协议是否处理完成，并发送实时语音流 userAgent.getMrcpStatus() 等于1 协商结束
 byte buffer[] = null;
 try {
        while (userAgent.getMrcpStatus()!=1){
            Thread.sleep(30);
        }
}

while (isRecording()) {
            buffer = audioBlockingQueue.poll();
            // status 为1 时候 mrcp 建立成功，可以写入数据了
            if(userAgent.getMrcpStatus()==1 && buffer!=null){
                userAgent.getConfig().getOutstream().write(buffer);
            }
        }

// 主动断开 hangup()
hangup(userAgent, sipRequest);


```


运行mrcpJavaClient


作者和联系方式
qq：  2637332218

致谢
。

![](audio/200-zixun.jpg)


支持一下作者

一毛我也要
一块也是肉
十块刚刚好
百元不得了
千元有点吊
万元岂止吊
十万吊中吊
百万嗷嗷叫
千万笑到老
一亿随便搞
十亿我尽孝
