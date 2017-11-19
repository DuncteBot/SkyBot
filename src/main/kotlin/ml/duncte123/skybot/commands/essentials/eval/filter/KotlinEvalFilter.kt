/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package ml.duncte123.skybot.commands.essentials.eval.filter

import Java.lang.VRCubeException
import groovy.lang.Closure
import groovy.lang.Script
import org.kohsuke.groovy.sandbox.GroovyValueFilter
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Arrays
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Kotlin version of the eval filter
 */
class KotlinEvalFilter : GroovyValueFilter() {

    /**
     * Filter the normal stuff
     */
	override fun filter(value: Any?): Any? {
        // If null, is null
		if(value == null) return null

        // Disallow the utilities in Script and the execution of Closures
		if(value is Script || value is Closure<*>)
            throw VRCubeException("Scrips and Closures are not allowed")

        // Disallow mentioning
        // Is String
        if(value is String)
            // Does match mention filter
            if(mentionFilter.matcher(value).find())
                // Error
                throw VRCubeException("**ERROR:** Mentioning people!")

        // If not allowed block it
		if(!filteredUsed.contains(value::class.java))
			throw VRCubeException("Class not allowed: ${value::class.qualifiedName}")

        // If it's not filtered, allow
		return value
	}

	/**
	 * We will just return it without any filtering
	 */
    override fun filterReceiver(receiver: Any?)= receiver

    /**
     * Disallow setting arrays
     */
	override fun onSetArray(invoker: Invoker, receiver: Any, index: Any, value: Any): Any {
		throw VRCubeException(
                "Cannot set array on $receiver, Class: ${receiver::class.java.componentType}, Index: $index, Value: $value")
	}

    /**
     * Filter the creation of new instances
     */
    override fun onNewInstance(invoker: Invoker, receiver: Class<*>, vararg args: Any?): Any {
        if(!filteredConstructed.contains(receiver))
            throw VRCubeException(
                    "Cannot create an instance of ${receiver.name}")

        // Simple mistake, the make the Object... take in the args as elements, we need to use *
        return invoker.call(receiver, null, *args)
    }
}

/*
 * These are outside of the class because they are static
 */

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

val mentionFilter = Pattern.compile("(<(@|@&)[0-9]{18}>)|@everyone|@here")!!
