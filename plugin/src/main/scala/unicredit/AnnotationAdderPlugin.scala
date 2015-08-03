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
  val components = List[PluginComponent](AnnotationAdderComponent)

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

  private object AnnotationAdderComponent extends PluginComponent {
    val global = AnnotationAdderPlugin.this.global
    import global._

    override val runsAfter = List("typer")

    val phaseName = "annotation-adder"

    override def newPhase(prev: Phase): StdPhase = new StdPhase(prev) {
      override def apply(unit: CompilationUnit) {
        
        config.foreach { (c: (String, String)) =>
          val clazz = STry {
            rootMirror.getClassByName((c._1: TypeName))
          }
          val annotation = STry {
            rootMirror.getClassByName((c._2: TypeName))
          }
          println("annotation "+annotation)
          (clazz, annotation) match {
            case (Success(classSym), Success(annotationSym)) =>
              classSym.addAnnotation(annotationSym)

              unit.warning(null, s"adding annotation ${c._2} to class ${c._1}")
            case _ =>
              unit.warning(null, s"ANNOTATION ADDER ERROR: ${c._1} or ${c._2} not found")
          }
        }
      }
    }
  }
}
