/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.core.action.builder

import akka.actor.ActorDSL.actor
import akka.actor.ActorRef
import io.gatling.core.action.If
import io.gatling.core.config.{ Protocol, Protocols }
import io.gatling.core.session.Expression
import io.gatling.core.structure.{ ScenarioContext, ChainBuilder }

/**
 * @constructor create a new IfBuilder
 * @param condition condition of the if
 * @param thenNext chain that will be executed if condition evaluates to true
 * @param elseNext chain that will be executed if condition evaluates to false
 */
class IfBuilder(condition: Expression[Boolean], thenNext: ChainBuilder, elseNext: Option[ChainBuilder]) extends ActionBuilder {

  def build(next: ActorRef, ctx: ScenarioContext) = {
    val safeCondition = condition.safe
    val thenNextActor = thenNext.build(next, ctx)
    val elseNextActor = elseNext.map(_.build(next, ctx)).getOrElse(next)
    actor(actorName("if"))(new If(safeCondition, thenNextActor, elseNextActor, next))
  }

  override def defaultProtocols: Set[Protocol] = {

    val actionBuilders = thenNext.actionBuilders ::: elseNext.map(_.actionBuilders).getOrElse(Nil)
    actionBuilders.flatMap(_.defaultProtocols).toSet
  }
}
