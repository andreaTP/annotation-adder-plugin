//package demo
//package unicredit
package demo.unicredit

import play.api.libs.json._

@com.kifi.macros.json case class Foo2(i: Int, s: String)
case class Foo3(i: Int, s: String)

object Demo2 extends App {
  val foo2 = Foo2(2, "hello foo2")
  val foo3 = Foo3(3, "hello foo3")

  val j1 = Json.prettyPrint(Json.toJson(foo2))
  val j2 = "demo"//Json.prettyPrint(Json.toJson(foo3))

  println(s"json1  $j1 \n and  json2 $j2")
}
