package com.stripe

import java.net.{URL, URLEncoder, HttpURLConnection}
import java.io.{InputStreamReader, BufferedReader, DataOutputStream}
import java.util.{HashMap, Map}
import java.lang.StringBuilder

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import scala.concurrent._
import ExecutionContext.Implicits.global

import com.stripe._
import com.stripe.exception._
import com.stripe.model._
import com.stripe.net._

import com.typesafe.config.ConfigFactory

import org.apache.oltu.oauth2._
import org.apache.oltu.oauth2.client._
import org.apache.oltu.oauth2.client.request._
import org.apache.oltu.oauth2.client.response._
import org.apache.oltu.oauth2.common.message.types._

object ScalaStripe {
  private lazy val config = ConfigFactory.load()
  private lazy val secretKey = config.getString("stripe.secret")

  Stripe.apiKey = secretKey

  def handleOauthValidationOltu(key: String) = {
    val url = "https://connect.stripe.com/oauth/token"
    val request = OAuthClientRequest
      .tokenLocation(url)
      .setGrantType(GrantType.AUTHORIZATION_CODE)
      .setParameter("client_secret", secretKey)
      .setCode(key)
      .buildBodyMessage()

    val headers = new HashMap[String, String]()
    headers.put("code", key)
    headers.put("Content-Type", "application/x-www-form-urlencoded")

    val urlConnectionClient = new URLConnectionClient()
    val oAuthResponse = urlConnectionClient.execute(
      request,
      headers,
      "POST",
      classOf[OAuthJSONAccessTokenResponse])

    println("Stripe response: "+ oAuthResponse.toString())
    oAuthResponse
  }

  def handleOauthValidation(authorizationCode: String) = {
    val STRIPE_AUTHORIZATION_LOCATION = "https://connect.stripe.com/oauth/token"
    val postString = "client_secret="+ secretKey +"&grant_type=authorization_code&code="+ authorizationCode

    val url = new URL(STRIPE_AUTHORIZATION_LOCATION)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setRequestProperty("Content-Type",  "application/x-www-form-urlencoded")
    connection.setDoOutput(true)
    connection.setDoInput(true)

    val os = new DataOutputStream(connection.getOutputStream())
    os.writeBytes(postString)
    os.flush()
    os.close()

    println("Posting to: "+ STRIPE_AUTHORIZATION_LOCATION)
    println("Parameters: "+ postString)
    println("Response: "+ connection.getResponseCode().toString)

    val is = new BufferedReader(new InputStreamReader(connection.getInputStream()))
    var line = is.readLine()
    val response = new StringBuilder()
    while (line != null) {
      response.append(line)
      line = is.readLine()
    }
    is.close()

    println("Stripe response: "+ response.toString())
  }


  def makeCharge(amount: Double, source: String, account: Option[String] = Some("acct_167aK7F9EZIqIj9k")) = {
    val chargeMap = new HashMap[String, Object]()
    chargeMap.put("amount", (amount * 100).toInt.toString)
    chargeMap.put("currency", "usd")
    chargeMap.put("source", source)
    chargeMap.put("application_fee", "123")
    /*val cardMap = new HashMap[String, Object]()
    cardMap.put("number", "4242424242424242");
    cardMap.put("exp_month", "12")
    cardMap.put("exp_year", "2020")
    chargeMap.put("card", cardMap)*/
    try {
      val charge = Charge.create(chargeMap, RequestOptions.builder().setStripeAccount(account.get).build())
      println(charge)
      charge
    } catch {
      case (e: StripeException) => e.printStackTrace()
    }
  }

  def makeRefund(id: String, account: Option[String] = Some("acct_167aK7F9EZIqIj9k")) = {
    val params = new HashMap[String, Object]()
    params.put("refund_application_fee", "true")
    val charge = Charge.retrieve({id}, RequestOptions.builder().setStripeAccount(account.get).build())
    val re = charge.getRefunds().create(params, RequestOptions.builder().setStripeAccount(account.get).build())
    println(re)

    re
  }

  def retrieveWebhook() = {

  }

  def createSubscription(amount: Int, interval: String, name: String, id: String, account: Option[String]) = {
    val planParams = new HashMap[String, Object]()
    planParams.put("amount", amount.toString)
    planParams.put("interval", interval)
    planParams.put("name", name)
    planParams.put("currency", "usd")
    planParams.put("id", id)

    account.fold(Plan.create(planParams)) {
      connectedAccount =>
      Plan.create(planParams, RequestOptions.builder().setStripeAccount(connectedAccount).build())
    }
  }


  def addPlan(custId: String, planName: String, account: Option[String] = None) = {
    val customer = retrieveCustomer(custId, account)
    val planMap = new HashMap[String, Object]()
    planMap.put("plan", planName)
    account.fold(customer.createSubscription(planMap)) {
      connectedAccount =>
      customer.createSubscription(planMap, RequestOptions.builder().setStripeAccount(connectedAccount).build())
    }
  }

  def retrieveCustomer(custId: String, account: Option[String] = None) =
    account.fold(Customer.retrieve(custId)) { connectedAccount =>
      Customer.retrieve(custId, RequestOptions.builder().setStripeAccount(connectedAccount).build())
    }

  def makeCustomer(email: String, token: String, account: Option[String] = None) = {
    val customerMap = new HashMap[String, Object]()
    customerMap.put("description", "customer for my gambling app")
    customerMap.put("source", token)
    //customerMap.put("email", email)
    try {
      val customer = account.fold(Customer.create(customerMap)) {
        connectedAccount =>
        Customer.create(customerMap, RequestOptions.builder().setStripeAccount(connectedAccount).build())
      }
      println(customer)
      customer
    } catch {
      case (e: StripeException) =>
        println(e)
        e.printStackTrace()
    }
  }

  trait ScalaAPIResource extends APIResource {
    def requestF[T](
      method: APIResource.RequestMethod,
      url: String,
      params: Map[String, Object],
      clazz: Class[T],
      options: RequestOptions
    ): Future[T] = Future {
      APIResource.request(method, url, params, clazz, options)
    }
  }


/*class ScalaCustomer extends Customer with ScalaAPIResource {

  def request(method: APIResource.RequestMethod, url: String, params: Map[String, Object], clazz: Class[FutureScalaCustomer]): Future[ScalaCustomer] = requestF[Future[ScalaCustomer]]()
 } */

}
