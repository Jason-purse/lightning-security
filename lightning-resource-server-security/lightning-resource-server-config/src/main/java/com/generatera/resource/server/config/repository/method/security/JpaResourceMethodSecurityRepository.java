package com.generatera.resource.server.config.repository.method.security;

import com.generatera.resource.server.config.model.entity.method.security.ResourceMethodSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaResourceMethodSecurityRepository extends JpaRepository<ResourceMethodSecurityEntity,String> {
}
