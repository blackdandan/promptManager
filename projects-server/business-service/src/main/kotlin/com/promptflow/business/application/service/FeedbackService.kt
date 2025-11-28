package com.promptflow.business.application.service

import com.promptflow.business.domain.model.Feedback
import com.promptflow.business.infrastructure.repository.FeedbackRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    fun submitFeedback(userId: String, type: String, content: String, contact: String?): Feedback {
        val feedback = Feedback(
            userId = userId,
            type = type,
            content = content,
            contact = contact
        )
        return feedbackRepository.save(feedback).also {
            log.info("收到用户 $userId 的反馈: ${it.id}")
        }
    }
}
