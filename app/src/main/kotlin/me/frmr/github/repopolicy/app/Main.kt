package me.frmr.github.repopolicy.app

import java.util.concurrent.Callable
import picocli.CommandLine.*

@Command(name="repo-policy", mixinStandardHelpOptions = true, version=["1.0"],
    description=["Validates or enforces a repo policy against GitHub repos"])
class AppMain: Callable<Int> {
  enum class Mode{
    validate, enforce
  }

  @Parameters(airty="1")
  lateinit var mode: Mode
  override fun call(): Int {
    TODO("Not yet implemented")
  }
}


fun main() {
  // TODO: Implement
}
