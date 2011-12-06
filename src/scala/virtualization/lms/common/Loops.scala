package scala.virtualization.lms
package common

import java.io.PrintWriter
import scala.virtualization.lms.internal.{GenericNestedCodegen,GenericFatCodegen}

trait Loops extends Base { // no surface constructs for now

}

trait LoopsExp extends Loops with BaseExp with EffectExp {

  case class IntRange(size: Exp[Int]) extends AbstractLoopRange[Int]
  
  abstract class AbstractLoop[A, Iter] extends Def[A] {
    val range: AbstractLoopRange[Iter]
    val v: Sym[Iter]
    val body: Def[A]
  }
  
  case class SimpleLoop[A, Iter](val range: AbstractLoopRange[Iter], val v: Sym[Iter], val body: Def[A]) extends AbstractLoop[A, Iter]
  
  def simpleLoop[A:Manifest, Iter](range: AbstractLoopRange[Iter], v: Sym[Iter], body: Def[A]): Exp[A] = SimpleLoop(range, v, body)
  def simpleLoop[A:Manifest](length: Exp[Int], v: Sym[Int], body: Def[A]): Exp[A] = SimpleLoop(IntRange(length), v, body)

  override def syms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractLoop[_,_] => syms(e.range) ::: syms(e.body) // should add super.syms(e) ?? not without a flag ...
    case _ => super.syms(e)
  }

	override def readSyms(e: Any): List[Sym[Any]] = e match { 
		case e: AbstractLoop[_,_] => readSyms(e.range) ::: readSyms(e.body)
    case _ => super.readSyms(e)
  }

  override def boundSyms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractLoop[_,_] => e.v :: boundSyms(e.body)
    case _ => super.boundSyms(e)
  }

  override def symsFreq(e: Any): List[(Sym[Any], Double)] = e match {
    case e: AbstractLoop[_,_] => freqNormal(e.range) ::: freqHot(e.body) // should add super.syms(e) ?? not without a flag ...
    case _ => super.symsFreq(e)
  }


	/////////////////////
  // aliases and sharing

  override def aliasSyms(e: Any): List[Sym[Any]] = e match {
		case e: AbstractLoop[_,_] => aliasSyms(e.body)
    case _ => super.aliasSyms(e)
  }

  override def containSyms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractLoop[_,_] => containSyms(e.body)
    case _ => super.containSyms(e)
  }

  override def extractSyms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractLoop[_,_] => extractSyms(e.body)
    case _ => super.extractSyms(e)
  }

  override def copySyms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractLoop[_,_] => copySyms(e.body)
    case _ => super.copySyms(e)
  }
}

trait LoopsFatExp extends LoopsExp with BaseFatExp {

  abstract class AbstractFatLoop extends FatDef {
    val range: AbstractLoopRange[Any]
    val v: Sym[Any]
    val body: List[Def[Any]]
  }
  
  case class SimpleFatLoop[Iter](val range: AbstractLoopRange[Iter], val v: Sym[Iter], val body: List[Def[Any]]) extends AbstractFatLoop


  override def syms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractFatLoop => syms(e.range) ::: syms(e.body)
    case _ => super.syms(e)
  }
  
  override def readSyms(e: Any): List[Sym[Any]] = e match { 
		case e: AbstractFatLoop => readSyms(e.range) ::: readSyms(e.body)
    case _ => super.readSyms(e)
  }

  override def boundSyms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractFatLoop => e.v :: boundSyms(e.body)
    case _ => super.boundSyms(e)
  }

  override def symsFreq(e: Any): List[(Sym[Any], Double)] = e match {
    case e: AbstractFatLoop => freqNormal(e.range) ::: freqHot(e.body)
    case _ => super.symsFreq(e)
  }

	/////////////////////
  // aliases and sharing

  override def aliasSyms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractFatLoop => aliasSyms(e.body)
    case _ => super.aliasSyms(e)
  }

  override def containSyms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractFatLoop => containSyms(e.body)
    case _ => super.containSyms(e)
  }

  override def extractSyms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractFatLoop => extractSyms(e.body)
    case _ => super.extractSyms(e)
  }

  override def copySyms(e: Any): List[Sym[Any]] = e match {
    case e: AbstractFatLoop => copySyms(e.body)
    case _ => super.copySyms(e)
  }
}




trait BaseGenLoops extends GenericNestedCodegen {
  val IR: LoopsExp
  import IR._

}

trait BaseGenLoopsFat extends BaseGenLoops with GenericFatCodegen {
  val IR: LoopsFatExp
  import IR._

  override def fatten(e: TP[Any]): TTP = e.rhs match {
    case op: AbstractLoop[_,_] => 
      TTP(List(e.sym), SimpleFatLoop(op.range, op.v, List(op.body)))
    case Reflect(op: AbstractLoop[_,_], u, es) if !u.maySimple && !u.mayGlobal => // assume body will reflect, too. bring it on...
      printdbg("-- fatten effectful loop " + e)
      TTP(List(e.sym), SimpleFatLoop(op.range, op.v, List(op.body)))
    case _ => super.fatten(e)
  }

}



trait ScalaGenLoops extends ScalaGenBase with BaseGenLoops {
  import IR._

  //TODO

}

trait ScalaGenLoopsFat extends ScalaGenLoops with ScalaGenFat with BaseGenLoopsFat {
  import IR._

  //TODO

}

trait CudaGenLoops extends CudaGenBase with BaseGenLoops {
  import IR._

  //TODO

}

trait CudaGenLoopsFat extends CudaGenLoops with CudaGenFat with BaseGenLoopsFat {
  import IR._

  //TODO

}

trait OpenCLGenLoops extends OpenCLGenBase with BaseGenLoops {
  import IR._

  //TODO

}

trait OpenCLGenLoopsFat extends OpenCLGenLoops with OpenCLGenFat with BaseGenLoopsFat {
  import IR._

  //TODO

}
