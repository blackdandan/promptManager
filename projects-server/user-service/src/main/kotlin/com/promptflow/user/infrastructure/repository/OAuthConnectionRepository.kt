package com.promptflow.user.infrastructure.repository

import com.promptflow.user.domain.model.OAuthConnection
import com.promptflow.user.domain.model.OAuthProvider
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OAuthConnectionRepository : MongoRepository<OAuthConnection, String> {
    
    fun findByUserId(userId: String): List<OAuthConnection>
    
    fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): Optional<OAuthConnection>
    
    @Query("{ 'user_id': ?0, 'provider': ?1 }")
    fun findByUserIdAndProvider(userId: String, provider: OAuthProvider): Optional<OAuthConnection>
    
    fun existsByUserIdAndProvider(userId: String, provider: OAuthProvider): Boolean
    
    fun existsByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): Boolean
    
    @Query("{ 'user_id': ?0 }")
    fun deleteByUserId(userId: String)
    
    @Query("{ 'user_id': ?0, 'provider': ?1 }")
    fun deleteByUserIdAndProvider(userId: String, provider: OAuthProvider)
}
