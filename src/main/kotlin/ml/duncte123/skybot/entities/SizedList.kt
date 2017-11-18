package ml.duncte123.skybot.entities

class SizedList<E>(private val fixedSize:Int) : ArrayList<E>() {

    override fun add(element: E): Boolean {
        if (this.size + 1 > fixedSize)
            return false
        return super.add(element)
    }

    override fun add(index: Int, element: E) {
        if (index > fixedSize || this.size + 1 > fixedSize )
            return
        super.add(index, element)
    }

    fun add(element: E, override: Boolean): Boolean {
        if (this.size + 1 > fixedSize && !override)
            return false
        else if (this.size + 1 > fixedSize && override) {
            removeAt(0)
            return this.add(element)
        }
        return add(element)
    }

    fun add(index: Int, element: E, override: Boolean) {
        if (index > fixedSize || (this.size + 1 > fixedSize && !override))
            return
        else if (this.size + 1 > fixedSize && override) {
            removeAt(index)
            this.add(index, element)
            return
        }
        add(index, element)
    }
}