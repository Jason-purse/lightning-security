# lightning-security
权限校验框架, 当前分为两个分类,一个是中央授权服务器,另一个是应用授权服务器 ..
## 中央授权服务器
例如 oauth2 授权流这样的服务器,客户端代表用户来请求资源信息..
- 客户端获取用户授权信息
- 客户端代表用户获取资源信息

本身并不是resource-server ...

## lightning-authorization-server-security
作为授权服务器的基本配置,基本想法是,让表单登录也能够进行token 颁发,撤销/和检测 ...
目前授权服务器主要想要遵循 spring oauth2的token(颁发 / 撤销 / 检测)规范
- 保留token 颁发 以及 authorization store
- token 撤销端点保留
- 公有的 token 相关默认配置(对于 oauth2来说, 默认是根据 客户端的token 配置进行处理)
- 共有的提供者信息
- jwkSource 提供(用于提供加密信息)
- token 生成器(用来生成token的,对于某些非前后端分离的应用授权服务器来说,这是没有必要的)



但是token检测是通过resource-server 进行处理的 ... 

## lightning-application-authorization-security
应用级的用户中心服务器 ..
一般来说,就是用户中心,主要是负责token的颁发,保留 spring oauth2的 token 颁发的一部分可以复用的组件进行处理 ..
但是它本身并不是resource-server
## lightning-resource-server-security
作为资源服务器的配置包,其中包括了遵循oauth2规范的资源服务器,其他简单登录的(例如前后端分离的表单)也使用遵循于oauth2 resource server的资源服务器jar 引用 ..
1. 主要本质上根据spring-oauth2 给出的 bearer token 作为 token的类型
2. bearer token 分为两种格式
   - 一种是 jwt token
   - 一种是 opaque token
3. 负责 token 的校验
  - 根据 authorization header 认证头进行 token 解析
    - jwt token 自解析
    - opaque token 请求授权服务器解析(根据响应决定处理)
4. 目前token 解析都是基于默认方式(也就是将 jwt 字符串的内容(claims)直接转换为一个 LightningUserPrincipal) 作为当前请求的 
已解析的 LightningUserContext中的用户信息,但是这一般不符合业务的实际要求,因为用户信息不是实际想要的 ...
所以需要自己实现 JwtClaimsToUserPrincipalMapper 进行 claims 到 LightningUserPrincipal的 处理 ...(这对于jwt / opaque来说)都是可以的,
详情查看javadoc 了解更多 ...
5. 目前plain-resource-server 直接引用了 oauth2-resource-server ...
## lightning-security-specifications
- token 生成的相关规范
  - 其中当前token 支持 bearer token
    其中包含两种形式(jwt / opaque)

## lightning-security-tests
各种服务器的基本测试,
目前各种服务器之间的结合使用并没有进行兼容性处理,或许后续打算处理,也不打算处理,因为很少会有这样需要兼容的情况,并且本质上不属于相同问题,也不应该解决 ..


## 快速开始
- 以表单登录为例
```text
lightning-application-authorization-form-login-server
```
结合
```text
lightning-security-application-authorization-form-login-security-test
```
进行快速启动,同时通过修改配置实现前后端分离配置/或者不分离配置(影响最大的就是 token是否生成) ...
- oauth2-login-client-server
```text
lightning-application-authorization-oauth2-login-client-server
```
它需要启动第三方oauth2 认证授权服务器,也就是google / qq / facebook,那么这些第三方厂商必然是存在的,我们自己的三方认证授权服务器可以查看
```text
lightning-central-oauth2-authorization-server
```
进行了解,同时通过
```text
lightning-central-authorization-pass-grant-support-server-test
```
快速开始启动一个oauth2 三方认证授权服务器 ...

## 测试页面
