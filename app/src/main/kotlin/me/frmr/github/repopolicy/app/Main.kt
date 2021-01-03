package me.frmr.github.repopolicy.app

import me.frmr.github.repopolicy.core.PolicyEngine
import java.util.concurrent.Callable
import picocli.CommandLine
import picocli.CommandLine.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

@Command(name="repo-policy", mixinStandardHelpOptions = true, version=["1.0"],
    description=["Validates or enforces a repo policy against GitHub repos"])
class Main: Callable<Int> {
  enum class Mode{
    validate, enforce
  }

  @Parameters(index="0", arity="1",
    description = ["Run mode. Valid options: \${COMPLETION-CANDIDATES}"])
  lateinit var mode: Mode

  @Parameters(index="1", arity = "1",
    description = ["YAML Policy file to run against GitHub"])
  lateinit var policyFile: Path;

  override fun call(): Int {
    println("Parsing policy file...")
    val file = Files.readString(policyFile)
    val engine = PolicyEngine(file)
    engine.initGithubClient()

    println(engine.policy)

    // Explicitly declared for clarity on what our
    // exit code means
    println("Running policy engine...")
    val fails = when (mode) {
      Mode.enforce -> PolicyEnforcementReporter.report(engine.enforce())
      Mode.validate -> PolicyValidationReporter.report(engine.validate())
    }
    return fails
  }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(Main()).execute(*args))
