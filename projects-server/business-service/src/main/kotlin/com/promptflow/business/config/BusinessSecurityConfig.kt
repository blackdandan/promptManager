package com.promptflow.business.config

import com.promptflow.config.SecurityConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SecurityConfig::class)
class BusinessSecurityConfig
