package pt.rmartins.dayofweek.main

import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.laminar.api.L.{u => _, _}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.{window, HTMLDivElement}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

object MainForm {

  private val currentDate: Var[Option[LocalDate]] = Var(None)
  private val selectedDayOfWeek: Var[Option[Int]] = Var(None)
  private val roundResult: Var[Option[Boolean]] = Var(None)
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
      .sample(selectedDayOfWeek, currentDate)
      .foreach {
        case (Some(selectedDayOfWeek), Some(currentDate)) =>
          val result = (currentDate.getDayOfWeek.getValue + 1 % 7) == selectedDayOfWeek
          Var.update(
            roundResult -> ((_: Option[Boolean]) => Some(result)),
            pointsVar -> ((points: Int) => if (result) points + 1 else points),
          )
        case _ =>
      }(owner)

    setNewDayOfWeek();

    div(
      className := "m-2",
      className := "d-flex justify-content-start align-items-center flex-column",
      h2(
        "Day of Week"
      ),
      h2(
        child.text <--
          currentDate.signal.map(_.map(dateFormatter.format(_)).getOrElse("")),
      ),
      dayOfWeeks.zipWithIndex.map { case (dayOfWeek, index) =>
        button(
          className := "btn m-1",
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
        className := "btn btn-success m-1",
        width.px := 120,
        "Confirm",
        disabled <-- selectedDayOfWeek.signal.combineWith(roundResult).map {
          case (None, _) | (_, Some(_)) => true
          case _                        => false
        },
        onClick.mapTo(()) --> resultCheckBus.writer,
      ),
      div(
        "Result: ",
        child.text <-- roundResult.signal.map {
          case None        => "---"
          case Some(true)  => "Correct"
          case Some(false) => "Incorrect"
        },
      ),
      button(
        className := "btn btn-primary m-1",
        width.px := 120,
        "Next Date",
        disabled <-- roundResult.signal.map(_.isEmpty),
        onClick --> (_ => setNewDayOfWeek()),
      ),
      div(
        "Points: ",
        child.text <-- pointsVar,
      )
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
