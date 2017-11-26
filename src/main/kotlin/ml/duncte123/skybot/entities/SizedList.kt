package ml.duncte123.skybot.entities

/**
 *
 * This class implements [List] and extends [ArrayList] and requires:
 * @param [fixedSize] the final size of the list.
 * [List.size] never can be bigger than [fixedSize]!
 * @author Sanduhr32
 */
class SizedList<E>(private val fixedSize:Int) : List<E>, ArrayList<E>() {

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
}