# oauth2-resource-server
## Token Kind
    总的来说OAuth2 目前使用的是  Bearer Token ...
### Token Type
    - JWT
    - Opaque Token

### 处理过程 .
    通过AuthenticationFilter 解决Token 解析问题
    所以非oauth2 resource server的情况,我们也应该尝试模仿它 ..