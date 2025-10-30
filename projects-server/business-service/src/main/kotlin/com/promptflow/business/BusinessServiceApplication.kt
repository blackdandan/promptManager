package com.promptflow.business

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class BusinessServiceApplication

fun main(args: Array<String>) {
    runApplication<BusinessServiceApplication>(*args)
}
