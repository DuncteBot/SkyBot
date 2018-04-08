package ml.duncte123.skybot.utils

class SpamCache : HashMap<Long, List<Long>>() {

    @Throws(IllegalArgumentException::class)
    public fun update(longs: LongArray, updateMode: Int = 0) {
        when {
            updateMode == -1 && longs.size == 1 -> {
                this - longs[0]
            }
            longs.size == 2 -> {
                val msgIds: List<Long> =
                        if (!this.containsKey(longs[0]))
                            ArrayList()
                        else
                            this[longs[0]] as ArrayList

                if (updateMode == 0) {
                    this + (longs[0] to msgIds.plus(longs[1]))
                }
                else if (updateMode == 1) {
                    this + (longs[0] to msgIds.minus(longs[1]))
                }
            }
            else -> {
                throw IllegalArgumentException("Arguments don't match.")
            }
        }
    }

}