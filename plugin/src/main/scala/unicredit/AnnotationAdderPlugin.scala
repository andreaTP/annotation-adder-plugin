package unicredit

import scala.tools.nsc.{ Global, Phase }
import scala.tools.nsc.plugins.{ Plugin, PluginComponent }
import scala.tools.nsc.transform.{ Transform, TypingTransformers }
import scala.tools.nsc.symtab.Flags
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.ast.TreeDSL

import java.nio.file.Files.readAllBytes
import java.nio.file.Paths.get

import scala.collection.mutable
import scala.util.{Try => STry, Success, Failure}

class AnnotationAdderPlugin(val global: Global) extends Plugin {
  import global._

  val name = "annotation-adder-plugin"
  val description = "Want to add annotation to classes, fields, and methods"
  val components = List[PluginComponent](AnnotationAdderComponent, AnnotationAdderCheckComponent)

  lazy val config: mutable.Set[(String, String)] = 
    (try new String(readAllBytes(get("./annotation_adder.config"))).split("\n").toSeq.map(e => {
       val splitted = e.split(" ")
       (splitted(0), splitted(1))
    })
     catch {
       case err: Throwable =>
         println("Annotation adder configuration file is missing")
         Seq()
     }).to[mutable.Set]

  private object AnnotationAdderCheckComponent extends PluginComponent {
    val global = AnnotationAdderPlugin.this.global
    import global._

    override val runsAfter = List("annotation-adder")
    override val runsRightAfter = Some("annotation-adder")

    val phaseName = "annotation-adder-check"

    override def newPhase(prev: Phase): StdPhase = new StdPhase(prev) {
      override def apply(unit: CompilationUnit) {
        config.foreach(x =>
          unit.warning(null, s"ANNOTATION ADDER ERROR:${x._1} not found")
        )
      }
    }

  }

  private object AnnotationAdderComponent extends PluginComponent  with Transform with TreeDSL {
    val global = AnnotationAdderPlugin.this.global
    import global._
    import global.definitions._

    override val runsAfter = List("typer")

    val phaseName = "annotation-adder"

    def newTransformer(unit: CompilationUnit) =
      new AggregateAddersTransformer(unit)
      
    class AggregateAddersTransformer(unit: CompilationUnit) extends Transformer {
      
      val adders = config.flatMap { (c: (String, String)) =>
        //first implementation only classes
        val clazz = STry {
          // TODO: we might select a method of an object
          val cl = rootMirror.getClassByName((c._1: TypeName))
          cl
        }
        clazz match {
          case Success(classSym) =>
            Seq(new AdderTransformer(unit, c._1, classSym, c._2))
          case Failure(e) =>
            println(s"class '${c._1}' does not exist")
            Seq()
        }
      } 

      override def transform(tree: Tree): Tree = {
        val iter = adders.iterator
        var count = 0
        while(iter.hasNext && !iter.next.check(tree)) {
          count += 1
        }
         //println(tree.getClass + " -> "+ tree)
        
        if (count == adders.size)
          super.transform(tree)
        else
          super.transform(tree)
          /*
          Literal(Constant(())) setType UnitTpe
          */
        
        //super.transform(tree)
      }
    }

    class AdderTransformer(unit: CompilationUnit, className: String, classSym: ClassSymbol, annotation: String) {

      def check(tree: Tree): Boolean = {
        tree match {
          case cd @ ClassDef(Modifiers(flags, privateWithin, annotations), name, tparams, impl)
            if (classSym == cd.symbol) =>
            //annotations.foreach(a =>
              
            //  println("RAW:\n"+showRaw(cd)+"\n")
            //)
            unit.warning(tree.pos, s"CLASS $className FOUND!!!\n annotations $annotations")
            
            false
          /*case any if (any.toString.contains("json") && showRaw(any).contains("Annotation")) =>
            println("Any on "+any)
            println("RAW:\n"+showRaw(any)+"\n")
            false*/
          case any =>
            //println(any.getClass)
            false
        }
      }
    }
  }
}
