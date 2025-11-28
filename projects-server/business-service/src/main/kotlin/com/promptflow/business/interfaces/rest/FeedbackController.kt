package com.promptflow.business.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.business.application.service.FeedbackService
import com.promptflow.business.interfaces.rest.dto.SubmitFeedbackRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/feedback")
class FeedbackController(
    private val feedbackService: FeedbackService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @PostMapping
    fun submitFeedback(
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: SubmitFeedbackRequest
    ): ResponseEntity<ApiResponse<Void>> {
        log.info("接收到用户反馈: userId=$userId, type=${request.type}")
        
        return try {
            feedbackService.submitFeedback(
                userId = userId,
                type = request.type,
                content = request.content,
                contact = request.contact
            )
            ResponseEntity.ok(ApiResponse.success(null, "反馈提交成功"))
        } catch (e: Exception) {
            log.error("提交反馈失败: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("FEEDBACK_SUBMIT_FAILED", "提交反馈失败"))
        }
    }
}
