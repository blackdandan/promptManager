package com.promptflow.user.infrastructure.repository

import com.promptflow.user.domain.model.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : MongoRepository<User, String> {
    
    fun findByEmail(email: String): Optional<User>
    
    fun findByUsername(username: String): Optional<User>
    
    @Query("{ 'status': ?0 }")
    fun findByStatus(status: String): List<User>
    
    @Query("{ 'user_type': ?0 }")
    fun findByUserType(userType: String): List<User>
    
    fun existsByEmail(email: String): Boolean
    
    fun existsByUsername(username: String): Boolean
    
    @Query("{ 'email': { \$regex: ?0, \$options: 'i' } }")
    fun findByEmailContaining(email: String): List<User>
    
    @Query("{ 'username': { \$regex: ?0, \$options: 'i' } }")
    fun findByUsernameContaining(username: String): List<User>
}
