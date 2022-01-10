关于 `thirdpart-login-custom`运行：
- 为国内访问方便 以 "gitee" 为例，在配置文件中替换自己的 `client-id` `client-secret`
- 运行前还需配置sql信息，由Flyway提供自动建表

`thirdpart-login-custom` 效果展示：
1. 访问受限资源 401
   ![](https://cdn.jsdelivr.net/gh/SimpleIto/blog-imgs/202201102125613.png)
2. 访问"/login"登录页，点击登录"Login With Gitee"，发起授权请求"https://gitee.com/oauth/authorize" ；
返回重定向响应，定向到实际跳到授权页。
授权后，gitee返回重定向响应，请求"redirect-url"，后端接受后返回code和昵称，前端根据code转入信息补全注册。
   ![](https://cdn.jsdelivr.net/gh/SimpleIto/blog-imgs/202201102132020.png)
3. （用POSTMAN模拟注册信息补全页）填了邮箱后发起注册请求，后端会合并Gitee信息完成系统用户注册，返回token。
   ![](https://cdn.jsdelivr.net/gh/SimpleIto/blog-imgs/202201102139952.png)
4. 使用token访问受限资源（无状态登录）
   ![](https://cdn.jsdelivr.net/gh/SimpleIto/blog-imgs/202201102140417.png)