package pt.rmartins.dayofweek.utils

import org.scalajs.dom

object LocalStorage {

  val PointsKey = "points"

  def storeValue(key: String, value: String): Unit = {
    dom.window.localStorage.setItem(key, value)
  }

  def retrieveValue(key: String): Option[String] = {
    Option(dom.window.localStorage.getItem(key))
  }

}
