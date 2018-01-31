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
inline infix fun <reified T> Array<out T>.random(action : (T) -> Unit): T {
    val t = this.random()
    action.invoke(t)
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
inline infix fun <reified T> List<T>.random(action : (T) -> Unit): T {
    val t = this.random()
    action.invoke(t)
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
inline infix fun <reified T> Set<T>.random(action : (T) -> Unit): T {
    val t = this.random()
    action.invoke(t)
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
inline infix fun <reified K, reified V> Map<K, V>.random(action: (V) -> Unit): V {
    val v = this.random()
    action.invoke(v)
    return v
}