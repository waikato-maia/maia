package māia.main

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
fun main() {

    val inputs = TestInputs(
        5,
        5,
        100_000,
        1,
        1,
        1,
        100,
        100,
        50,
        false
    )

    val moa = measure(::MOASourceTest, inputs, HandleErrorsBy.THROW)
    val moana = measure(::MOANASourceTest, inputs, HandleErrorsBy.THROW)

    println("MOA: $moa")
    println("MOANA: $moana")

}

fun testSource(inputs: TestInputs, source: () -> Any?) {
    for (index in 0 until inputs.maxInstances) {
        printlnmulti(
            index,
            System.identityHashCode(source())
        )
    }
}

fun MOASourceTest(inputs : TestInputs) {
    val source = moaSource(inputs)

    testSource(inputs, source::nextInstance)
}

fun MOANASourceTest(inputs: TestInputs) {
    val source = moanaSource(inputs).rowIterator()

    testSource(inputs, source::next)
}
