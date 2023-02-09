package com.generatera.resource.server.config.repository.method.security;

import com.generatera.resource.server.config.model.entity.method.security.ResourceMethodSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaResourceMethodSecurityRepository extends JpaRepository<ResourceMethodSecurityEntity,String> {

    public List<ResourceMethodSecurityEntity> findAllByMethodSecurityIdentifierIn(
            List<String> identifiers
    );
}
