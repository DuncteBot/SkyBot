/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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
 */

package ml.duncte123.skybot.commands.essentials.eval.filter

import Java.lang.VRCubeException
import groovy.lang.Closure
import groovy.lang.Script
import ml.duncte123.skybot.entities.delegate.*
import org.kohsuke.groovy.sandbox.GroovyValueFilter
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Arrays
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.List
import kotlin.collections.listOf

class KotlinEvalFilter : GroovyValueFilter() {
    
    /**
     * Typed that are allowed to be used
     */
    private val filteredUsed = listOf(
            String::class.java,
            Math::class.java,
            
            Boolean::class.java,
            BooleanArray::class.java,
            Byte::class.java,
            ByteArray::class.java,
            Char::class.java,
            CharArray::class.java,
            Short::class.java,
            ShortArray::class.java,
            Integer::class.java,
            IntArray::class.java,
            Float::class.java,
            FloatArray::class.java,
            Long::class.java,
            LongArray::class.java,
            Double::class.java,
            DoubleArray::class.java,
            
            Arrays::class.java,
            
            List::class.java,
            ArrayList::class.java,
            
            BigDecimal::class.java,
            BigInteger::class.java)
    
    /**
     * Types that are allowed to be constructed
     */
    private val filteredConstructed = listOf(
            String::class.java,
            Math::class.java,
            
            Boolean::class.java,
            BooleanArray::class.java,
            Byte::class.java,
            ByteArray::class.java,
            Char::class.java,
            CharArray::class.java,
            Short::class.java,
            ShortArray::class.java,
            Integer::class.java,
            IntArray::class.java,
            Float::class.java,
            FloatArray::class.java,
            Long::class.java,
            LongArray::class.java,
            Double::class.java,
            DoubleArray::class.java,
            
            Arrays::class.java,
            
            java.util.List::class.java,
            List::class.java,
            ArrayList::class.java,
            HashSet::class.java,
            
            // Want to add these?
            // Can have huge radix
            // BigDecimal::class.java,
            // BigInteger::class.java,
            
            GuildDelegate::class.java,
            JDADelegate::class.java,
            MemberDelegate::class.java,
            UserDelegate::class.java,
            PresenceDelegate::class.java
    )
    
    override fun filter(v: Any?): Any? {
        var value: Any? = KotlinMappingFilter.transform(v)
        if (value == null) return null
        
        // Allow delegates
        if (value::class.java.`package`.name == "ml.duncte123.skybot.entities.delegate")
            return value;
        
        if (value is Script || value is Closure<*>)
            throw VRCubeException("Scrips and Closures are not allowed")
        
        if (!filteredUsed.contains(value::class.java))
            throw VRCubeException("Class not allowed: ${value::class.qualifiedName}")
        
        return value
    }
    
    override fun filterReceiver(receiver: Any?) = receiver
    
    override fun onSetArray(invoker: Invoker, receiver: Any, index: Any, value: Any): Any {
        throw VRCubeException(
                "Cannot set array on $receiver, Class: ${receiver::class.java.componentType}, Index: $index, Value: $value")
    }
    
    override fun onNewInstance(invoker: Invoker, receiver: Class<*>, vararg args: Any?): Any {
        if (!filteredConstructed.contains(receiver))
            throw VRCubeException(
                    "Cannot create an instance of ${receiver.name}")
        
        return invoker.call(receiver, null, *args)
    }
}
