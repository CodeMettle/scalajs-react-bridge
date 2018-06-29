package com.payalabs.scalajs.react

import japgolly.scalajs.react.component.Js
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, VdomNode}
import japgolly.scalajs.react.{CallbackTo, Children, CtorType}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.scalajs.js
import scala.scalajs.js.JSConverters.{JSRichFutureNonThenable, JSRichOption}
import scala.scalajs.js.{Object, |}


package object bridge extends GeneratedImplicits {
  def writerFromConversion[A](implicit conv: A => js.Any): JsWriter[A] = JsWriter(x => x)
  implicit def stringWriter: JsWriter[String] = writerFromConversion[String]

  implicit def boolWriter: JsWriter[Boolean] = writerFromConversion[Boolean]

  // Note: Char and Long not supported here, since in Scala.js they map to opaque types, which
  // may not map well to the underlying components expectations.
  implicit def byteWriter: JsWriter[Byte] = writerFromConversion[Byte]
  implicit def shortWriter: JsWriter[Short] = writerFromConversion[Short]
  implicit def intWriter: JsWriter[Int] = writerFromConversion[Int]

  implicit def floatWriter: JsWriter[Float] = writerFromConversion[Float]
  implicit def doubleWriter: JsWriter[Double] = writerFromConversion[Double]
  implicit def numberWriter: JsWriter[Number] =
    writerFromConversion(implicitly[Double => js.Any].compose[Number](_.doubleValue))

  implicit def unitWriter: JsWriter[Unit] = writerFromConversion[Unit]
  implicit def jsAnyWriter[A <: js.Any]: JsWriter[A] = JsWriter(identity)

  implicit def callbackToWriter[T](implicit writerT: JsWriter[T]): JsWriter[CallbackTo[T]] =
    JsWriter(value => value.map(writerT.toJs).runNow())

  implicit def undefOrWriter[A](implicit writerA: JsWriter[A]): JsWriter[js.UndefOr[A]] =
    JsWriter(_.map(writerA.toJs))

  implicit def optionWriter[A](implicit writerA: JsWriter[A]): JsWriter[Option[A]] =
    JsWriter(_.map(writerA.toJs).orUndefined)

  implicit def unionWriter[A : ClassTag, B : ClassTag](implicit writerA: JsWriter[A], writerB: JsWriter[B]): JsWriter[A | B] =
    JsWriter({
      case value: A => writerA.toJs(value)
      case value: B => writerB.toJs(value)
    })

  implicit def enumerationWriter[T <: Enumeration#Value]: JsWriter[T] =
    JsWriter(_.toString)

  implicit def arrayWriter[T : JsWriter]: JsWriter[scala.Array[T]] = {
    val elementWriter = implicitly[JsWriter[T]]

    JsWriter((value: scala.Array[T]) => js.Array(value.map(e => elementWriter.toJs(e)): _*))
  }

  implicit def seqWriter[T: JsWriter]: JsWriter[Seq[T]] = {
    val elementWriter = implicitly[JsWriter[T]]

    JsWriter((value: Seq[T]) => js.Array(value.map(e => elementWriter.toJs(e)): _*))
  }

  implicit def immutableSeqWriter[T : JsWriter]: JsWriter[scala.collection.immutable.Seq[T]] = {
    val elementWriter = implicitly[JsWriter[T]]

    JsWriter((value: scala.collection.immutable.Seq[T]) => js.Array(value.map(e => elementWriter.toJs(e)): _*))
  }

  implicit def mapWriter[T : JsWriter]: JsWriter[Map[String, T]] = {
    val elementWriter = implicitly[JsWriter[T]]

    JsWriter(
      (value: Map[String, T]) => {
        val converted = value.map { case (k, v) => (k, elementWriter.toJs(v)) }
        js.Dictionary(converted.toSeq: _*)
      }
    )
  }

  implicit def futureWriter[A](implicit writeA: JsWriter[A], executionContext: ExecutionContext): JsWriter[Future[A]] =
    JsWriter(_.map(writeA.toJs).toJSPromise)

  implicit def vdomElementWriter: JsWriter[VdomElement] = JsWriter(_.rawElement)

  /*
  implicit def reactNodeWriter: JsWriter[React.Node] = {
    lazy val arrayWriter: JsWriter[js.Array[_]] = JsWriter((a: js.Array[_]) => a.map(e => writer.toJs(e)))

    lazy val writer = JsWriter[Any]({
      case null => nullWriter.toJs(null)
      case () => unitWriter.toJs(())
      case b: Boolean => boolWriter.toJs(b)
      case b: Byte => byteWriter.toJs(b)
      case s: Short => shortWriter.toJs(s)
      case i: Int => intWriter.toJs(i)
      case f: Float => floatWriter.toJs(f)
      case d: Double => doubleWriter.toJs(d)
      case a: js.Array[_] => arrayWriter.toJs(a)
      case e: js.Object => e
    })

    writer.asInstanceOf[JsWriter[React.Node]]
  }*/

  implicit def vdomNodeWriter: JsWriter[VdomNode] = JsWriter[VdomNode](n => n.rawNode.asInstanceOf[js.Any])

  type JsComponentType = Js.ComponentSimple[Object, CtorType.Summoner.Aux[Object, Children.Varargs, CtorType.PropsAndChildren]#CT, Js.UnmountedWithRawType[Object, Null, Js.RawMounted[Object, Null]]]

  def extractPropsAndChildren(attrAndChildren: Seq[TagMod]): (js.Object, List[VdomNode]) = {
    import japgolly.scalajs.react.vdom.Implicits._

    val b = new japgolly.scalajs.react.vdom.Builder.ToJs {}
    attrAndChildren.toTagMod.applyTo(b)
    b.addClassNameToProps()
    b.addStyleToProps()

    (b.props, b.childrenAsVdomNodes)
  }


}
