/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package me.frmr.github.app

import me.frmr.github.utilities.StringUtils

fun main() {
    val tokens = StringUtils.split(MessageUtils.getMessage())
    println(StringUtils.join(tokens))
}
