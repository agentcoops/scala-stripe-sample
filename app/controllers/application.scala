package controllers

import com.stripe.ScalaStripe

import play.api._
import play.api.mvc._

class Application extends Controller {
  import play.api.Play.current

  def makeCharge = Action { implicit request =>
    println(request)
    try {
      val form = request.body.asFormUrlEncoded.get
      println(form)
      val charge = ScalaStripe.makeCharge(20.1, form("stripeToken").head)
      println(charge)
    } catch {
      case (e: Throwable) => Ok("No form...")
    }
    Ok("You did it!")
  }

  def printRequest = Action { implicit request =>
    try {
      val form = request.body.asFormUrlEncoded.get
      println(form)
    } catch {
      case (e: Throwable) => Ok("No form...")
    }
    Ok("You did it!")
  }

  def connectOauth(key: String) = Action { implicit request =>
    println(key)
    //val key = "ac_6eMyP7RA5FSmQHApwKCGKibCpW97YGc2"
    ScalaStripe.handleOauthValidation(key)

    Ok("Oauth oauth oauth")
  }

  def makeCustomer = Action { implicit request =>
    println(request)
    try {
      val form = request.body.asFormUrlEncoded.get
      println(form)
      val customer = ScalaStripe.makeCustomer(form("stripeEmail").head, form("stripeToken").head)
      println(customer)
    } catch {
      case (e: Throwable) => Ok("No form...")
    }
    Ok("You did it!")
  }
}
