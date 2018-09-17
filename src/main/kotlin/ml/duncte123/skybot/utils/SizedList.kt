/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.utils

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot

/**
 *
 * This class implements [List] and extends [ArrayList] and requires:
 * @param [fixedSize] the final size of the list.
 * [List.size] never can be bigger than [fixedSize]!
 * @author Sanduhr32
 */
@SinceSkybot("3.51.5")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
class SizedList<E>(private val fixedSize: Int) : List<E>, ArrayList<E>() {

    /**
     *
     * @param [element] the element that should be added.
     * @throws [UnsupportedOperationException] if the list is full.
     */
    override fun add(element: E): Boolean {
        if (this.size + 1 > fixedSize)
            throw UnsupportedOperationException("You cant add an element because the list is \"full\"!")
        return super.add(element)
    }

    /**
     *
     * @param [index] of the element that should be added.
     * @param [element] the element that should be added.
     * @throws [UnsupportedOperationException] if the list is full.
     * @throws [IndexOutOfBoundsException] if the index is larger than the list.
     */
    override fun add(index: Int, element: E) {
        if (index > fixedSize)
            throw IndexOutOfBoundsException("Index cant be greater than the fixed size!\nYour index: $index, the fixed size: $fixedSize")
        if (this.size + 1 > fixedSize)
            throw UnsupportedOperationException("You cant add an element because the list is \"full\"!")
        super.add(index, element)
    }

    /**
     *
     * @param [element] the element that should be added.
     * @param [override] if the value should be overwritten if the list is full.
     * @throws [UnsupportedOperationException] if the list is full.
     * @throws [IndexOutOfBoundsException] if the index is larger than the list.
     */
    fun add(element: E, override: Boolean): Boolean {
        if (this.size + 1 > fixedSize && !override)
            throw UnsupportedOperationException("You cant add an element because the list is \"full\" and you dont override one value!")
        else if (this.size + 1 > fixedSize && override) {
            removeAt(0)
            return this.add(element)
        }
        return add(element)
    }

    /**
     *
     * @param [index] of the element that should be added.
     * @param [element] the element that should be added.
     * @param [override] if the value should be overwritten if the list is full.
     * @throws [UnsupportedOperationException] if the list is full.
     * @throws [IndexOutOfBoundsException] if the index is larger than the list.
     */
    fun add(index: Int, element: E, override: Boolean) {
        if (index > fixedSize)
            throw IndexOutOfBoundsException("Index cant be greater than the fixed size!\nYour index: $index, the fixed size: $fixedSize")
        if (this.size + 1 > fixedSize && !override)
            throw UnsupportedOperationException("You cant add an element because the list is \"full\" and you dont override one value!")
        if (this.size + 1 > fixedSize && override) {
            this[index] = element
            return
        }
        add(index, element)
    }

    override fun toString(): String {
        return "[Maximum size: $fixedSize, current size: $size, content: ${super.toString()}]"
    }
}