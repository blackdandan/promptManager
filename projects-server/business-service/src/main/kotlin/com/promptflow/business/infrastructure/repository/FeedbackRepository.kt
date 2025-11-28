package com.promptflow.business.infrastructure.repository

import com.promptflow.business.domain.model.Feedback
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedbackRepository : MongoRepository<Feedback, String>
