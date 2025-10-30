package com.promptflow.membership

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class MembershipServiceApplication

fun main(args: Array<String>) {
    runApplication<MembershipServiceApplication>(*args)
}
