@file:Suppress("MagicNumber")

package com.hitchy.core.ui

import com.hitchy.core.data.AppError
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.error_internal
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ErrorCatalogTest {

    data class ErrorEntry(
        val reason: String,
        val english: String,
        val arabic: String,
    )

    private fun findRepoRoot(): File {
        var current: File? = File(".").canonicalFile
        while (current != null) {
            if (File(current, "contract/errors.md").exists()) {
                return current
            }
            current = current.parentFile
        }
        error("Could not find repo root with contract/errors.md from ${File(".").canonicalPath}")
    }

    private fun parseErrorsContract(contractFile: File): List<ErrorEntry> {
        val lines = contractFile.readLines()
        return lines.mapNotNull { line ->
            val trimmed = line.trim()
            if (!trimmed.startsWith("|") || !trimmed.endsWith("|")) return@mapNotNull null
            val parts = trimmed.split("|").map { it.trim() }
            if (parts.size < 5) return@mapNotNull null
            val rawReason = parts[1]
            if (!rawReason.startsWith("`") || !rawReason.endsWith("`")) return@mapNotNull null

            val reason = rawReason.removeSurrounding("`")
            if (reason.isEmpty() || reason == "Reason") return@mapNotNull null

            val english: String
            val arabic: String
            if (parts[2].startsWith("`")) {
                // Table 2: | Reason | HTTPS Code | English | Arabic |
                english = parts[3]
                arabic = parts[4]
            } else {
                // Table 3: | Reason | English | Arabic | Note |
                english = parts[2]
                arabic = parts[3]
            }

            ErrorEntry(reason = reason, english = english, arabic = arabic)
        }
    }

    private fun parseStringsXml(file: File): Map<String, String> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(file)
        val stringNodes = doc.getElementsByTagName("string")
        val map = mutableMapOf<String, String>()
        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            val name = node.attributes?.getNamedItem("name")?.nodeValue ?: continue
            val rawContent = node.textContent ?: ""
            // Android XML escapes apostrophes and quotes: \' -> ', \" -> "
            val unescaped = rawContent.replace("\\'", "'").replace("\\\"", "\"")
            map[name] = unescaped
        }
        return map
    }

    @Test
    fun contractCatalogContainsAll71Reasons() {
        val root = findRepoRoot()
        val contractFile = File(root, "contract/errors.md")
        val entries = parseErrorsContract(contractFile)
        assertEquals(71, entries.size, "contract/errors.md should contain 71 error reasons")
    }

    @Test
    fun allReasonsExistInStringsXmlAndMatchContractExactly() {
        val root = findRepoRoot()
        val contractFile = File(root, "contract/errors.md")
        val entries = parseErrorsContract(contractFile)

        val enFile = File(root, "android/shared/src/commonMain/composeResources/values/strings.xml")
        val arFile = File(root, "android/shared/src/commonMain/composeResources/values-ar/strings.xml")
        assertTrue(enFile.exists(), "values/strings.xml must exist")
        assertTrue(arFile.exists(), "values-ar/strings.xml must exist")

        val enStrings = parseStringsXml(enFile)
        val arStrings = parseStringsXml(arFile)

        for (entry in entries) {
            val key = "error_${entry.reason.lowercase()}"
            assertTrue(enStrings.containsKey(key), "Missing English string key '$key' for ${entry.reason}")
            assertTrue(arStrings.containsKey(key), "Missing Arabic string key '$key' for ${entry.reason}")

            assertEquals(
                entry.english,
                enStrings[key],
                "English text mismatch for key '$key' (${entry.reason})",
            )
            assertEquals(
                entry.arabic,
                arStrings[key],
                "Arabic text mismatch for key '$key' (${entry.reason})",
            )
        }
    }

    @Test
    fun errorTextMappingCoversEveryReason() {
        val root = findRepoRoot()
        val contractFile = File(root, "contract/errors.md")
        val entries = parseErrorsContract(contractFile)

        for (entry in entries) {
            val error = AppError.Reason(code = entry.reason)
            val resource = error.message()
            if (entry.reason != "INTERNAL") {
                assertNotEquals(
                    Res.string.error_internal,
                    resource,
                    "Reason ${entry.reason} mapped to fallback error_internal instead of dedicated resource",
                )
            }
        }
    }
}
