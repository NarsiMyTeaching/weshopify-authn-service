package com.weshopify.platform.features.authn.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weshopify.platform.featuresauthn.models.UserRole;

public interface UserRolesDataRepo extends JpaRepository<UserRole, Integer> {

}
