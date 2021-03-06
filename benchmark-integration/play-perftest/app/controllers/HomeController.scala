package controllers

import io.opentelemetry.OpenTelemetry
import io.opentelemetry.trace.Tracer
import javax.inject.Inject

import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's work page which does busy wait to simulate some work
  */
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  val TRACER: Tracer = OpenTelemetry.getTracerProvider.get("io.opentelemetry.auto")

  /**
    * Create an Action to perform busy wait
    */
  def doGet(workTimeMS: Option[Long], error: Option[String]) = Action { implicit request: Request[AnyContent] =>
    error match {
      case Some(x) => throw new RuntimeException("some sync error")
      case None => {
        var workTime = workTimeMS.getOrElse(0l)
        scheduleWork(workTime)
        Ok("Did " + workTime + "ms of work.")
      }
    }

  }

  private def scheduleWork(workTimeMS: Long) {
    val span = TRACER.spanBuilder("work").startSpan()
    val scope = TRACER.withSpan(span)
    try {
      if (span != null) {
        span.setAttribute("work-time", workTimeMS)
        span.setAttribute("info", "interesting stuff")
        span.setAttribute("additionalInfo", "interesting stuff")
      }
      if (workTimeMS > 0) {
        Worker.doWork(workTimeMS)
      }
    } finally {
      span.end()
      scope.close()
    }
  }
}
