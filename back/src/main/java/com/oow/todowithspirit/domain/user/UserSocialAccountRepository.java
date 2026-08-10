package com.oow.todowithspirit.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {

    @Query("SELECT sa FROM UserSocialAccount sa JOIN FETCH sa.user WHERE sa.provider = :provider AND sa.providerUserId = :providerUserId")
    Optional<UserSocialAccount> findByProviderAndProviderUserId(
            @Param("provider") OAuthProvider provider,
            @Param("providerUserId") String providerUserId
    );
}