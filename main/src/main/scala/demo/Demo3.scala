//package demo
//package unicredit
package demo.unicredit

import scala.scalajs.js.JSApp

import scala.scalajs.js
import js.annotation.JSExport

@JSExport case class Foo2(i: Int, s: String)
case class Foo3(i: Int, s: String)
case class Foo4(i: Int, s: String)

object Demo2 extends JSApp {

	def main(): Unit = {
		println("Hello world!")	
	}
	
}
