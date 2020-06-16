import java.time._

import com.google.common.collect.{Range, TreeRangeSet}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

object Main extends App {

  // The normal opening hours. On some days, the shop is closed (so
  // so there are no opening hours. Other days have several ranges
  // (closed for lunch break, ..)
  // We do not use the Guava "Range" class here because we are working with
  // java.time.LocalTime, which cannot represent "end of day" (24:00).
  val normalOpeningHours = Map(
    DayOfWeek.MONDAY    -> Seq(range(time(10, 0), duration(2, 0)),
                               range(time(14, 0), duration(5, 0))),
    DayOfWeek.TUESDAY   -> Seq(range(time(10, 0), duration(4, 0))),
    DayOfWeek.WEDNESDAY -> Seq(range(time(14, 0), duration(5, 0))),
    DayOfWeek.THURSDAY  -> Seq(range(time(10, 0), duration(4, 0))),
    DayOfWeek.FRIDAY    -> Seq(range(time(14, 0), duration(5, 0))),
    DayOfWeek.SATURDAY  -> Seq(range(time(10, 0), duration(2, 0)))
  )

  // two examples of additional opening (or closed) hours
  val overrideOpeningHours = List(
    date(2017, 5, 1) -> Seq(range(time(0, 0), duration(0,0))),   // closed on 1st of may
    date(2017, 5, 7) -> Seq(range(time(10, 0), duration(14, 0))) // a sunday opening
  )

  // the actual days that we want to know the opening hours for. May 2017 in this case.
  val dateRange: Seq[LocalDate] = (1 to 31) map (date(2017, 5, _))

  // convert the normal opening hours (LocalTime + Duration) into Range objects
  val normalRanges = for {
    date  <- dateRange
    range <- normalOpeningHours getOrElse (date.getDayOfWeek, Seq())
    from  =  LocalDateTime of (date, range.from)
    to    =  from plus range.duration
  } yield Range.closedOpen(from, to)

  // convert the override opening hours (LocalTime + Duration) into Range objects
  val overrideRanges = for {
    (date, ranges)  <- overrideOpeningHours
    range           <- ranges
    from            =  LocalDateTime of (date, range.from)
    to              =  from plus range.duration
  } yield Range.closedOpen(from, to)

  // using Guava, we cannot get around mutable objects always, so here we use a
  // mutable TreeRangeSet.
  val mutableRS = TreeRangeSet.create[LocalDateTime]()
  (normalRanges ++ overrideRanges).foldLeft(mutableRS) { (rs, range) =>
    range match {
      case r if r.isEmpty => // whole day closed, so we _remove_ the corresponding range
        rs.remove(Range.closedOpen(r.lowerEndpoint(), r.lowerEndpoint() plusDays 1))
      case r => // add the open range
        rs.add(r)
    }
    rs
  }

  // get back to scala and immutable objects, sorting by the way
  val calculatedRanges = mutableRS.asDescendingSetOfRanges.asScala.toList.reverse


  calculatedRanges.foreach { r =>
    println(r)
  }

  case class OpeningHours(from: LocalTime, duration: Duration)

  private def date(year: Int, month: Int, day: Int) = LocalDate.of(year, month, day)

  private def range(from: LocalTime, duration: Duration) = OpeningHours(from, duration)

  private def time(h: Int, m: Int) = LocalTime.of(h, m)

  private def duration(h: Int, m: Int) = Duration.ofMinutes(h * 60 + m)
}
