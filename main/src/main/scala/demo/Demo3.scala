//package demo
//package unicredit
package demo.unicredit

import scala.scalajs.js.JSApp

import scala.scalajs.js
import js.annotation._

@JSExport case class Foo2 (i: Int, s: String) {
	def fooDef() = "s is "+s+" i is "+i

	val fooField= (for (k <- 0 until i) yield s).mkString("[",",","]")
}
case class Foo3(i: Int, s: String)
case class Foo4(i: Int, s: String)

@JSExportDescendentClasses(ignoreInvalidDescendants = true)
abstract class AbstractFoo

	case class Foo6(i: Int, s: String) extends AbstractFoo

	protected[unicredit] case class Foo5(i: Int, s: String) extends AbstractFoo

object Demo2 extends JSApp {

	def main(): Unit = {
		println("Hello world!")	
	}
	
}
