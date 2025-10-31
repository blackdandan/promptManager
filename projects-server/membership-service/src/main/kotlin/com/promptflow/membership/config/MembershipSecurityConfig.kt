package com.promptflow.membership.config

import com.promptflow.config.SecurityConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SecurityConfig::class)
class MembershipSecurityConfig
