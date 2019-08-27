### Java nio 写的一个简单rpc例子
### 通过代理远程访问服务端的接口
### 使用TLC(Type Length Content)格式定义报文
### 支持服务端重启, 客户端重连重试机制
### 增加方法上添加日志注解功能
### \src\main\java\com\dll\sockets\server\Server.java  服务提供节点
### \src\main\java\com\dll\sockets\client\Client.java  服务消费节点
### \src\main\java\com\dll\sockets\protocol\ReadHandler.java 解析socket报文的handler
### \src\main\java\com\dll\sockets\context\Context.java 简单的Bean注册中心
### \src\main\java\com\dll\sockets\protocol\BusHandler.java  请求逻辑base handler
### \src\main\java\com\dll\sockets\protocol\ReturnBusHandler.java 返回逻辑base handler
### \src\main\java\com\dll\sockets\test 下面俩个测试case: ClientMain 为客户端, ServerMain服务端
