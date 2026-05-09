package com.aiide

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

class MultiFileCrossValidator(private val context: Context) {

    private val validationLock = ReentrantReadWriteLock()

    data class ValidationResult(
        val fileCount: Int,
        val issues: List<ValidationIssue>,
        val crossReferences: List<CrossReference>,
        val overallScore: Double
    )

    data class ValidationIssue(
        val severity: IssueSeverity,
        val message: String,
        val filePath: String?,
        val lineNumber: Int?
    )

    enum class IssueSeverity { ERROR, WARNING, INFO }

    data class CrossReference(
        val fromFile: String,
        val toFile: String,
        val referenceType: String,
        val lineNumber: Int
    )

    fun validate(files: Map<String, String>): ValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        val crossRefs = mutableListOf<CrossReference>()

        val symbols = extractSymbols(files)
        val unresolved = findUnresolvedReferences(files, symbols)

        for ((file, content) in files) {
            issues.addAll(checkSyntax(content, file))
            issues.addAll(checkImports(content, file, files))
            crossRefs.addAll(extractCrossReferences(content, file))
        }

        for (ref in unresolved) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.ERROR,
                message = "Unresolved reference: ${ref.symbol}",
                filePath = ref.file,
                lineNumber = ref.line
            ))
        }

        val score = computeOverallScore(issues, files.size)

        return ValidationResult(
            fileCount = files.size,
            issues = issues,
            crossReferences = crossRefs,
            overallScore = score
        )
    }

    private data class Symbol(val name: String, val file: String, val line: Int)
    private data class UnresolvedRef(val symbol: String, val file: String, val line: Int)

    private fun extractSymbols(files: Map<String, String>): List<Symbol> {
        val symbols = mutableListOf<Symbol>()
        val classRegex = Regex("""class\s+(\w+)""")
        val funcRegex = Regex("""fun\s+(\w+)""")
        val valRegex = Regex("""val\s+(\w+)""")
        val varRegex = Regex("""var\s+(\w+)""")

        for ((file, content) in files) {
            classRegex.findAll(content).forEach { symbols.add(Symbol(it.groupValues[1], file, 0)) }
            funcRegex.findAll(content).forEach { symbols.add(Symbol(it.groupValues[1], file, 0)) }
            valRegex.findAll(content).forEach { symbols.add(Symbol(it.groupValues[1], file, 0)) }
            varRegex.findAll(content).forEach { symbols.add(Symbol(it.groupValues[1], file, 0)) }
        }

        return symbols
    }

    private fun findUnresolvedReferences(files: Map<String, String>, symbols: List<Symbol>): List<UnresolvedRef> {
        val unresolved = mutableListOf<UnresolvedRef>()
        val symbolNames = symbols.map { it.name }.toSet()

        for ((file, content) in files) {
            val refRegex = Regex("""\b(\w+)\b""")
            refRegex.findAll(content).forEach { match ->
                val name = match.groupValues[1]
                if (name !in symbolNames && name.first().isUpperCase()) {
                    unresolved.add(UnresolvedRef(name, file, 0))
                }
            }
        }

        return unresolved
    }

    private fun checkSyntax(content: String, file: String): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()

        val braceCount = content.count { it == '{' } - content.count { it == '}' }
        if (braceCount != 0) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.ERROR,
                message = "Mismatched braces: difference of $braceCount",
                filePath = file,
                lineNumber = null
            ))
        }

        val parenCount = content.count { it == '(' } - content.count { it == ')' }
        if (parenCount != 0) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.ERROR,
                message = "Mismatched parentheses: difference of $parenCount",
                filePath = file,
                lineNumber = null
            ))
        }

        return issues
    }

    private fun checkImports(content: String, file: String, allFiles: Map<String, String>): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val importRegex = Regex("""import\s+([\w.]+)""")

        importRegex.findAll(content).forEach { match ->
            val importPath = match.groupValues[1]
            if (!importPath.startsWith("java.") && !importPath.startsWith("kotlin.") && !importPath.startsWith("android.")) {
                val localImport = importPath.substringAfterLast(".")
                val hasLocalClass = allFiles.values.any { content ->
                    Regex("""class\s+$localImport""").containsMatchIn(content)
                }
                if (!hasLocalClass) {
                    issues.add(ValidationIssue(
                        severity = IssueSeverity.WARNING,
                        message = "Import may not be used: $importPath",
                        filePath = file,
                        lineNumber = null
                    ))
                }
            }
        }

        return issues
    }

    private fun extractCrossReferences(content: String, fromFile: String): List<CrossReference> {
        val refs = mutableListOf<CrossReference>()
        val refRegex = Regex("""(\w+)\.""")

        refRegex.findAll(content).forEach { match ->
            refs.add(CrossReference(
                fromFile = fromFile,
                toFile = match.groupValues[1],
                referenceType = "method_call",
                lineNumber = 0
            ))
        }

        return refs
    }

    private fun computeOverallScore(issues: List<ValidationIssue>, fileCount: Int): Double {
        if (fileCount == 0) return 0.0

        val errorCount = issues.count { it.severity == IssueSeverity.ERROR }
        val warningCount = issues.count { it.severity == IssueSeverity.WARNING }

        val penalty = errorCount * 10 + warningCount * 2
        return (100.0 - penalty).coerceIn(0.0, 100.0)
    }

    fun toJson(result: ValidationResult): String = JSONObject().apply {
        put("fileCount", result.fileCount)
        put("issues", JSONArray().apply {
            result.issues.forEach { issue ->
                put(JSONObject().apply {
                    put("severity", issue.severity.name)
                    put("message", issue.message)
                    put("filePath", issue.filePath)
                    put("lineNumber", issue.lineNumber)
                })
            }
        })
        put("crossReferences", JSONArray().apply {
            result.crossReferences.forEach { ref ->
                put(JSONObject().apply {
                    put("fromFile", ref.fromFile)
                    put("toFile", ref.toFile)
                    put("referenceType", ref.referenceType)
                    put("lineNumber", ref.lineNumber)
                })
            }
        })
        put("overallScore", result.overallScore)
    }.toString()
}
