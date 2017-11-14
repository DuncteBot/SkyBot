package ml.duncte123.skybot.commands.essentials.eval.filter

import org.kohsuke.groovy.sandbox.GroovyValueFilter
import java.math.BigInteger
import java.math.BigDecimal
import java.util.Arrays

import ml.duncte123.skybot.exceptions.VRCubeException
import groovy.lang.Script
import groovy.lang.Closure

class KotlinEvalFilter() : GroovyValueFilter() {

    /**
     * Typed that are allowed to be used
     */
	val filteredUsed = listOf(
			String::class.java,
            Math::class.java,
			
            Boolean::class.java,
            Byte::class.java,
            Character::class.java,
            Short::class.java,
            Integer::class.java,
            Float::class.java,
            Long::class.java,
            Double::class.java,
            
            Arrays::class.java,
            
            List::class.java,
            ArrayList::class.java,
            
            BigDecimal::class.java,
            BigInteger::class.java)

    /**
     * Types that are allowed to be constructed
     */
    val filteredConstructed = listOf(
            String::class.java,
            Math::class.java,

            Boolean::class.java,
            Byte::class.java,
            Character::class.java,
            Short::class.java,
            Integer::class.java,
            Float::class.java,
            Long::class.java,
            Double::class.java,

            Arrays::class.java,

            ArrayList::class.java,
            HashSet::class.java,

            // Want to add these?
            // Can have huge radix
            BigDecimal::class.java,
            BigInteger::class.java
    )

	override fun filter(value: Any?): Any? {
		if(value == null) return null

		if(value is Script || value is Closure<*>)
			throw VRCubeException("Scrips and Closures are not allowed")

		if(!filteredUsed.contains(value::class.java))
			throw VRCubeException("Class not allowed: ${value::class.qualifiedName}")

		return value
	}

    override fun filterReceiver(receiver: Any?)= receiver

	override fun onSetArray(invoker: Invoker, receiver: Any, index: Any, value: Any): Any {
		throw VRCubeException(
                "Cannot set array on $receiver, Class: ${receiver::class.java.componentType}, Index: $index, Value: $value")
	}

    override fun onNewInstance(invoker: Invoker, receiver: Class<*>, vararg args: Any?): Any {
        if(receiver != null)
            if(!filteredConstructed.contains(receiver))
                throw VRCubeException(
                        "Cannot create an instance of ${receiver.name}")

        return invoker.call(receiver, null, args)
    }
}