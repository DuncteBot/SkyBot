/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot

import java.util.*

/**
 * This function gets an random object of the [Array] based on the [Array.size] using [Random.nextInt]
 *
 * @returns an random object of the [Array] matching the type [T]
 */
@SinceSkybot("3.57.7")
inline fun <reified T> Array<out T>.random(): T {
    return this[Random().nextInt(this.size)]
}

/**
 * This function gets an random object of the [Array] based on the [Array.size] using [Random.nextInt]
 * and executes the lambda accepting the random object.
 *
 * @returns an random object of the [Array] matching the type [T]
 */
@SinceSkybot("3.57.8")
inline infix fun <reified T> Array<out T>.random(action: T.() -> Unit): T {
    val t = this.random()
    action(t)
    return t
}

/**
 * This function gets an random object of the [List] based on the [List.size] using [Random.nextInt]
 *
 * @returns an random object of the [List] matching the type [T]
 */
@SinceSkybot("3.57.7")
inline fun <reified T> List<T>.random(): T {
    return this[Random().nextInt(this.size)]
}

/**
 * This function gets an random object of the [List] based on the [List.size] using [Random.nextInt]
 * and executes the lambda accepting the random object.
 *
 * @returns an random object of the [List] matching the type [T]
 */
@SinceSkybot("3.57.8")
inline infix fun <reified T> List<T>.random(action: T.() -> Unit): T {
    val t = this.random()
    action(t)
    return t
}

/**
 * This function gets an random object of the [Set] based on the [Set.elementAt] using [Set.size] and [Random.nextInt]
 *
 * @returns an random object of the [Set] matching the type [T]
 */
@SinceSkybot("3.57.7")
inline fun <reified T> Set<T>.random(): T {
    return this.elementAt(Random().nextInt(this.size))
}

/**
 * This function gets an random object of the [Set] based on the [Set.elementAt] using [Set.size] and [Random.nextInt]
 * and executes the lambda accepting the random object.
 *
 * @returns an random object of the [Set] matching the type [T]
 */
@SinceSkybot("3.57.8")
inline infix fun <reified T> Set<T>.random(action: T.() -> Unit): T {
    val t = this.random()
    action(t)
    return t
}

/**
 * This function gets an random object of the [Map] based on [Map.keys] and [Set.random]
 *
 * @returns an random object of the [Map]
 */
@SinceSkybot("3.57.7")
inline fun <reified K, reified V> Map<K, V>.random(): V {
    return this[this.keys.random()]!!
}

/**
 * This function gets an random object of the [Map] based on [Map.keys] and [Set.random]
 * and executes the lambda accepting the random object.
 *
 * @returns an random object of the [Map]
 */
@SinceSkybot("3.57.8")
inline infix fun <reified K, reified V> Map<K, V>.random(action: V.() -> Unit): V {
    val v = this.random()
    action(v)
    return v
}