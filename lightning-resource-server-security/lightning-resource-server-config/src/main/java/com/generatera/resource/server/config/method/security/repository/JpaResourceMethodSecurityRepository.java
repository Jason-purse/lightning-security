package com.generatera.resource.server.config.method.security.repository;

import com.generatera.resource.server.config.method.security.entity.ResourceMethodSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaResourceMethodSecurityRepository extends JpaRepository<ResourceMethodSecurityEntity,String> {

    public List<ResourceMethodSecurityEntity> findAllByMethodSecurityIdentifierIn(
            List<String> identifiers
    );
}
