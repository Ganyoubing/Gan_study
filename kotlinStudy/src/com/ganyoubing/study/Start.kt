package com.ganyoubing.study

import com.sun.deploy.util.DeploySysRun.execute
import javafx.scene.input.TransferMode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Start {
    fun test(str : String?){

    }


    fun foo(x: Int) : Int {return x*2}
    fun foo1(x: Int) = x*2

        fun find(a: Int) {
        if (a in 1..100 step 1) {//范围表达
        }


    }

    fun sum(x: Int, y: Int) = x + y
    val sum = {x: Int,y: Int -> x + y}

    val comparator =object : Comparator<String> {
        override fun compare(o1: String?, o2: String?): Int {
            if (o1 == null)
                return -1
            else if (o2 == null)
                return 1
            return o1.compareTo(o2)
        }
    }

    val comparator2 = Comparator<String> {o1, o2 ->
        if (o1 == null)
            return@Comparator -1
        else if (o2 == null)
            return@Comparator 1
        o1.compareTo(o2)
    }
    //Worker 并发
//    val future = execute(TransferMode.MOVE,{})  {
//
//    }




}
class STatr{

}
//内联类
inline class Name(val s: String)



fun foo() {
    val a = "start"  // = val a: String = "start"

    val list1 = arrayListOf(1,2,3,4,5,6,7)
    val list2 = arrayListOf(5,6,7,8,9,10,11)

    //val list = list1 - list2 第一种写法

    val list = list1.filter { it !in list2 }
    listOf(1,2,3).forEach { print(it)}

    list.apply {  }
    with(list) {

    }

    println(list)




    for (index in 0 until 100000) {

    }


}
fun main()  {
    var index = 0
    val list = mutableListOf<String>()

    runBlocking {
        repeat(100_000) {//循环100000次
            GlobalScope.launch {//开启一个线程
//                print("world! \n")
                list.add("1")
                index++

            }
            index++
//            delay(2000)
        }
        println("count : $index")
        println("count2 : "+ list.size)
        delay(10000)
    }
}


