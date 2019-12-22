# proxy
a proxy server and client GUI by java

用java 实现的http/https的代理服务器，包含客户端和服务器程序

服务器端  
&emsp;（1）在指定端口（例如 9080）接收来自客户的 http/https 请求并且根据其中的 URL 地址访问该地址所指向的 http/https 服务器（原服务器），接收服务器的响应报文，并将响应报文转发给对应的客户进行浏览。  
&emsp;（2）支持日志功能，可以将用户的访问目标和内容记录到指定的文件。  
&emsp;（3）网站过滤：允许/不允许重点内容访问某些网站  
&emsp;（4）多级代理功能，可以指定上级代理服务器实现多级代理

客户端

&emsp;实现设置IE代理的GUI界面
