# lightning-application-authorization-server-com.generatera.oauth2.resource.server.config
主要是配置引导 授权服务器启动的模板配置,支持的授权服务器包括:
1. 表单登录
2. oauth2 授权登录

## 遵循约定
基于oauth2的 provider / token  / resource-server 的公共约定,我们支持
1. 非oauth2 授权服务器可以提供自己的provider 配置
2. 基于 jwt的token 派发
3. 基于resource-server 进行 token校验