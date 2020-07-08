package com.ganyoubing.study

open class Person {
    private val birds: List<Bird>? = null

    /**
     * 1.3新加的新特性
     * @JvmField消除了变量的getter与setter方法
     * @JvmField修饰的变量不能是private属性的
     * @JvmStatic只能在object类或者伴生对象companion object中使用，而@JvmField没有这些限制
     * @JvmStatic一般用于修饰方法，使方法变成真正的静态方法；如果修饰变量不会消除变量的getter与setter方法，但会使getter与setter方法和变量都变成静态
    */
    companion object {
        //在 “companion object” 中的公共函数必须用使用 @JvmStatic 注解才能暴露为静态方法。
        @JvmStatic //静态方法  @JvmField 静态变量
        fun main(args: Array<String>): Unit {
            val person = Person()
            person.birds?.forEach {
                println(it.color?.length)
            }
            person.let {
                print(it.birds)

            }
            with(person) {

            }
            person.apply {

            }
            person.also {

            }
            person.run {

            }
        }
    }

    fun foo() {
        val brid = Bird(1.0,10,"10")
        brid.let {
            it.weight
            it.age
            it.color
        }
    }
}

fun main() {
    val brid = Bird(1.0,10,"red")
    print(brid.let {
        it.weight
        it.age
        it.color
    })

    print(brid.apply {
        this.weight
        this.age
        this.color
    })

}