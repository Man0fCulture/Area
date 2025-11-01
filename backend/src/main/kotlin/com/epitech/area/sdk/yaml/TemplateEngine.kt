package com.epitech.area.sdk.yaml

import com.hubspot.jinjava.Jinjava
import com.jayway.jsonpath.JsonPath
import org.bson.Document

/**
 * Moteur de templates pour YAML services
 * Permet d'utiliser {{ variable }} dans les URLs, bodies, etc.
 */
object TemplateEngine {
    private val jinjava = Jinjava()

    /**
     * Rendre un template avec des variables
     * Exemple: "Hello {{name}}" + {name: "World"} → "Hello World"
     */
    fun render(template: String, variables: Map<String, Any?>): String {
        return try {
            jinjava.render(template, variables)
        } catch (e: Exception) {
            template // Retourne le template original en cas d'erreur
        }
    }

    /**
     * Rendre un template avec config + triggerData
     */
    fun render(template: String, config: Map<String, String>, triggerData: Document = Document()): String {
        val allVars = mutableMapOf<String, Any?>()
        allVars.putAll(config.mapKeys { "config.${it.key}" })
        allVars.putAll(triggerData.mapKeys { "trigger.${it.key}" })
        allVars["config"] = config
        allVars["trigger"] = triggerData
        return render(template, allVars)
    }

    /**
     * Extraire une valeur depuis JSON avec JSONPath
     * Exemple: $.messages[0].id
     */
    fun extractJsonPath(json: String, jsonPath: String): Any? {
        return try {
            JsonPath.read<Any>(json, jsonPath)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extraire plusieurs champs depuis JSON
     */
    fun extractFields(json: String, outputs: List<OutputField>): Document {
        val result = Document()
        outputs.forEach { output ->
            output.jsonPath?.let { path ->
                val value = extractJsonPath(json, path)
                result[output.name] = value
            }
        }
        return result
    }

    /**
     * Vérifier une condition (expression simple)
     * Exemple: "$.count > 0"
     */
    fun evaluateCondition(json: String, condition: String): Boolean {
        return try {
            // Extraire la valeur avec JSONPath
            val parts = condition.split(Regex("\\s+"))
            if (parts.size >= 3) {
                val jsonPath = parts[0]
                val operator = parts[1]
                val expected = parts.subList(2, parts.size).joinToString(" ").trim('"', '\'')

                val actual = extractJsonPath(json, jsonPath)

                when (operator) {
                    ">" -> (actual as? Number)?.toDouble()?.let { it > expected.toDoubleOrNull() ?: 0.0 } ?: false
                    "<" -> (actual as? Number)?.toDouble()?.let { it < expected.toDoubleOrNull() ?: 0.0 } ?: false
                    "==" -> actual.toString() == expected
                    "!=" -> actual.toString() != expected
                    "contains" -> actual.toString().contains(expected, ignoreCase = true)
                    else -> false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
