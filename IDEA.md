还可发展的方向（欢迎提交Pr）：

- 目前暂未支持Interceptor、Filter、Servlet等内存马检测算法。
- 目前可以支持Attach Agent，但是还未实现Self Attach JVM的方式运行。
- 目前Sink函数只有Runtime.exec，还可以添加其他恶意函数进行检测。
- 添加Agent启动参数，用于控制是否自动删除内存马/是否开启DumpClass功能