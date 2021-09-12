package me.frmr.github.repopolicy.app

import me.frmr.github.repopolicy.core.PolicyEngine
import java.util.concurrent.Callable
import picocli.CommandLine
import picocli.CommandLine.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * Main class. Largely a wrapper around picocli
 */
@Command(name="repo-policy", mixinStandardHelpOptions = true,
    description=["Validates or enforces a repo policy against GitHub repos"])
class Main: Callable<Int> {
  /** Mode the engine should run in */
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
    System.err.println("Parsing policy file...")
    val file = Files.readString(policyFile)
    val engine = PolicyEngine(file, true)
    engine.initGithubClient()

    // Explicitly declared for clarity on what our
    // exit code means
    System.err.println("Running policy engine...")
    val fails = when (mode) {
      Mode.enforce -> ConsolePolicyEnforcementReporter.report(engine.enforce())
      Mode.validate -> ConsolePolicyValidationReporter.report(engine.validate())
    }
    return fails
  }
}

/**
 * Main metod that invokes pico.
 */
fun main(args: Array<String>): Unit = exitProcess(CommandLine(Main()).execute(*args))
