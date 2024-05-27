package pt.rmartins.dayofweek.main

import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.laminar.api.L.{u => _, _}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLDivElement
import pt.rmartins.dayofweek.utils.LocalStorage
import pt.rmartins.dayofweek.utils.LocalStorage.retrieveValue

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

object MainForm {

  private val currentDate: Var[Option[LocalDate]] = Var(None)
  private val selectedDayOfWeek: Var[Option[Int]] = Var(None)
  private val roundResult: Var[Option[(Boolean, Int)]] = Var(None)
  private val pointsVar: Var[Int] = Var(0)

  private val resultCheckBus: EventBus[Unit] = new EventBus

  private val minDate = LocalDate.of(1700, 1, 1)
  private val maxDate = LocalDate.of(2399, 12, 31)
  private val dayOfWeeks: IndexedSeq[String] =
    IndexedSeq("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

  private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

  def apply(): ReactiveHtmlElement[HTMLDivElement] = {
    val owner = new OneTimeOwner(() => ())

    resultCheckBus.events
      .sample(selectedDayOfWeek, currentDate, pointsVar)
      .foreach {
        case (Some(selectedDayOfWeek), Some(currentDate), currentPoints) =>
          val expectedDayOfWeek = currentDate.getDayOfWeek.getValue % 7
          val result = expectedDayOfWeek == selectedDayOfWeek
          val newPoints = if (result) currentPoints + 1 else currentPoints
          Var.set(
            roundResult -> Some((result, expectedDayOfWeek)),
            pointsVar -> newPoints,
          )
          if (result)
            LocalStorage.storeValue(LocalStorage.PointsKey, newPoints.toString)
        case _ =>
      }(owner)

    setNewDayOfWeek()

    println((LocalStorage.PointsKey, retrieveValue(LocalStorage.PointsKey)))

    Var.set(
      pointsVar -> retrieveValue(LocalStorage.PointsKey).map(_.toInt).getOrElse(0),
    )

    div(
      className := "m-2",
      className := "d-flex justify-content-start align-items-center flex-column",
      h3(
        "Day of Week"
      ),
      div(
        "Points: ",
        child.text <-- pointsVar,
      ),
      h1(
        className := "my-2",
        child.text <--
          currentDate.signal.map(_.map(dateFormatter.format(_)).getOrElse("")),
      ),
      dayOfWeeks.zipWithIndex.map { case (dayOfWeek, index) =>
        button(
          className := "btn mb-1",
          className <-- selectedDayOfWeek.signal.map(selected =>
            if (selected.contains(index)) "btn-primary" else "btn-outline-primary"
          ),
          width.px := 120,
          dayOfWeek,
          disabled <-- roundResult.signal.map(_.nonEmpty),
          onClick.mapTo(Some(index)) --> selectedDayOfWeek.writer,
        )
      },
      button(
        className := "btn btn-secondary m-1",
        width.px := 120,
        "Confirm",
        disabled <-- selectedDayOfWeek.signal.combineWith(roundResult).map {
          case (None, _) | (_, Some(_)) => true
          case _                        => false
        },
        onClick.mapTo(()) --> resultCheckBus.writer,
      ),
      span(
        className <--
          roundResult.signal.map {
            case None             => ""
            case Some((true, _))  => "text-success"
            case Some((false, _)) => "text-danger"
          },
        "Result: ",
        child <-- roundResult.signal.map {
          case None =>
            span("---")
          case Some((true, _)) =>
            span(b("Correct"), i(className := "text-success fa-solid fa-check ps-1"))
          case Some((false, _)) =>
            span(b("Incorrect"))
        },
        visibility <-- roundResult.signal.map(result =>
          if (result.isEmpty) visibility.hidden.value else visibility.visible.value
        ),
      ),
      span(
        child <-- roundResult.signal.map {
          case None            => span()
          case Some((true, _)) => span()
          case Some((false, correctAnswer)) =>
            span(className("text-success"), "It's ", b(s"${dayOfWeeks(correctAnswer)}"))
        },
        visibility <-- roundResult.signal.map(result =>
          if (result.isEmpty) visibility.hidden.value else visibility.visible.value
        ),
      ),
      button(
        className := "btn btn-primary m-1",
        width.px := 120,
        "Next Date ",
        i(className := "fa-solid fa-chevron-right ps-1"),
        visibility <-- roundResult.signal.map(result =>
          if (result.isEmpty) visibility.hidden.value else visibility.visible.value
        ),
        onClick --> (_ => setNewDayOfWeek()),
      ),
    )
  }

  private def setNewDayOfWeek(): Unit = {
    val minEpochDay = minDate.toEpochDay
    val maxEpochDay = maxDate.toEpochDay
    val randomEpochDay = minEpochDay + Random.nextLong((maxEpochDay - minEpochDay) + 1)
    Var.set(
      currentDate -> Some(LocalDate.ofEpochDay(randomEpochDay)),
      selectedDayOfWeek -> None,
      roundResult -> None,
    )
  }

}
