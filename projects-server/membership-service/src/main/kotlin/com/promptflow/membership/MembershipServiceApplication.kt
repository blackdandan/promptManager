package com.promptflow.membership

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = ["com.promptflow.membership", "com.promptflow.config"])
class MembershipServiceApplication

fun main(args: Array<String>) {
    runApplication<MembershipServiceApplication>(*args)
}
