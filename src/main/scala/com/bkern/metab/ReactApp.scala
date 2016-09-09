package com.bkern.metab

import com.bkern.metab.components.SiteContainer
import com.bkern.metab.css.AppCSS
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import japgolly.scalajs.react._

@JSExport
object ReactApp extends JSApp {

  @JSExport
  override def main(): Unit = {
    AppCSS.load
    SiteContainer() render dom.document.getElementById("main")

  }

}

