package io.github.jokoroukwu.zephyrng.config

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.constructor.StandardConstructor
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.Tag
import java.util.regex.Pattern


val ENV_OR_PROP_FORMAT: Pattern =
    Pattern.compile("""^\$\{\s*(?<name>[\w.]+)(?:(?<separator>:?[-?])(?<value>[\w.]+)?)?\s*}$""")

/**
 * Adds support to use system properties along with environment variables
 * as substitutions in a YAML document.
 */
class SystemPropResolvingConstructor(settings: LoadSettings) : StandardConstructor(settings) {

    init {
        tagConstructors[Tag.ENV_TAG] = SystemPropertyConstruct()
    }

    inner class SystemPropertyConstruct : StandardConstructor.ConstructEnv() {

        override fun construct(node: Node): Any = (node as ScalarNode).value.let {
            settings.envConfig.map { envConfig ->
                with(ENV_OR_PROP_FORMAT.matcher(it)) {
                    find()
                    val name = group("name")
                    val separator = group("separator")
                    val value = group("value") ?: ""
                    val env = getEnv(name)
                    envConfig.getValueFor(name, separator, value, env)
                        .orElseGet { apply(name, separator, value, env) }
                }
            }.orElse(it)
        }
    }
}
