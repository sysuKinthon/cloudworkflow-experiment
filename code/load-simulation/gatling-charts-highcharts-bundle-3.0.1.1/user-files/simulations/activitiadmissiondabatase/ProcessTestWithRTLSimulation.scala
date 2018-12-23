package activitiadmissiondatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import activitiadmissiondatabase.process._

class ProcessTestWithRTLSimulation extends 
Simulation {
	val httpConf = http
		.baseUrl("http://222.200.180.59:8774/")
	
	def basictest() {
		var contentType = Map("Content-Type" -> "application/x-www-form-urlencoded")
		var scn = scenario("basic activiti scenario")
			.exec { session =>
				session.set("processDefinitionId", "leave:9:52536")
			}
			.exec(http("startProcessInstanceById")
				.post("/startProcessInstanceById/${processDefinitionId}")
				.headers(contentType)
				.formParam("apply", "zhangsan")
				.formParam("approve", "lisi")
				.check(jsonPath("$..processInstanceId").saveAs("processInstanceId")))
			.pause(3)
			.exec(http("getCurrentSingleTask")
				.get("getCurrentSingleTask/${processInstanceId}")
				.check(jsonPath("$..taskId").saveAs("taskId")))
			.pause(1)
			.exec {session => 
				println(session)
				session
			}
		setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
	}

	def process_leavemodel_pass() {
		var leavePass = scenario("leave pass").exec(Process_LeaveModel_PASS.workflow)
		setUp(leavePass.inject(atOnceUsers(1)).protocols(httpConf))
	}

	def process_leavemodel_notpass() {
		var leaveNotPass = scenario("leave no pass").exec(Process_LeaveModel_NOTPASS_N.workflow)
		setUp(leaveNotPass.inject(atOnceUsers(1)).protocols(httpConf))
	}

	def process_onlineshoppingmodel() {
		var onlineshopping = scenario("online shopping").exec(Process_OnlineShoppingModel.workflow)
		setUp(onlineshopping.inject(atOnceUsers(1)).protocols(httpConf))
	}

	def process_auto_onlineShoppingModel() {
		var auto_onlineShopping = scenario("auto online shopping").exec(Process_Auto_OnlineShoppingModel.workflow)
		setUp(
			auto_onlineShopping.inject(
				constantUsersPerSec(10) during(5 minutes))
		).throttle(
			reachRps(10) in (10 seconds),
			holdFor(1 minute),
			jumpToRps(5),
			holdFor(2 minute)
		).protocols(httpConf)
	}

	def process_auto_onlineShoppingModel2() {
		var auto_onlineShopping = scenario("auto online shopping2").exec(Process_Auto_OnlineShoppingModel2.workflow)
		setUp(
			auto_onlineShopping.inject(
				rampUsers(600) during (2 minutes))
		).throttle(
			reachRps(60) in (5 seconds),
			holdFor(1 minute)
		).protocols(httpConf)
	}


	// basictest()
	// process_leavemodel_pass()
	// process_leavemodel_notpass()
	// process_onlineshoppingmodel()
	process_auto_onlineShoppingModel2()

}