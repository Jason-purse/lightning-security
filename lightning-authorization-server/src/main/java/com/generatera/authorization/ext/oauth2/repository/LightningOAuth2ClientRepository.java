//package com.generatera.authorization.ext.oauth2.repository;
//
//import com.generatera.authorization.ext.oauth2.entity.OAuth2ClientEntity;
//import org.springframework.data.repository.CrudRepository;
//
//import java.util.Optional;
//
///**
// * @author FLJ
// * @date 2022/12/27
// * @time 17:27
// * @Description oauth2 client repository
// */
//public interface LightningOAuth2ClientRepository extends CrudRepository<OAuth2ClientEntity,Long> {
//    /**
//     * 根据client id 查询 oauth2 client 实体
//     * @param clientId client
//     * @return oauth2 client entity
//     */
//    Optional<OAuth2ClientEntity> findFirstByClientId(String clientId);
//}
