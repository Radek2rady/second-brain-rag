package org.example.secondbrainrag

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class SecondBrainRagApplication

fun main(args: Array<String>) {
    runApplication<SecondBrainRagApplication>(*args)
}
