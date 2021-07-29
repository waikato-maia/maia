package māia.main

import māia.util.createType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class Test {}

fun Test.bla() : String {
    return "bla $this"
}

fun bla2(i : Test) : String {
    return "bla2 $i"
}

fun main() {

    val t = createType<Test.() -> String>(listOf(
            KTypeProjection.invariant(Test::class.createType()),
            KTypeProjection.invariant(String::class.createType())
    ))
    val t2 = createType<(Test) -> String>(listOf(
            KTypeProjection.invariant(Test::class.createType()),
            KTypeProjection.invariant(String::class.createType())
    ))
    val t3 = Test::bla::class.createType(listOf(
            KTypeProjection.invariant(Test::class.createType()),
            KTypeProjection.invariant(String::class.createType())
    ))
    println(t == t2)

}
