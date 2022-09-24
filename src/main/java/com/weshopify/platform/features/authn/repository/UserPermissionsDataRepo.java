package com.weshopify.platform.features.authn.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weshopify.platform.featuresauthn.models.UserPermissions;

public interface UserPermissionsDataRepo extends JpaRepository<UserPermissions, Integer> {

}
