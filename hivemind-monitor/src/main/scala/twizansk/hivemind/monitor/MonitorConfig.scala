package twizansk.hivemind.monitor

import com.typesafe.config.Config
import twizansk.hivemind.api.model.BatchObjectiveFunction
import twizansk.hivemind.api.model.Model
import twizansk.hivemind.common.ActorLookupFactory
import twizansk.hivemind.common.DefaultActorLookupFactory

/**
 * Config class for the Monitor actor.
 */
object MonitorConfig {
  def createConfig(config: Config) : MonitorConfig = {
    val objectiveClassName = config.getString("hivemind.drone.objective")
    val objectiveClass = Class.forName(objectiveClassName)
    val objective = objectiveClass.newInstance().asInstanceOf[BatchObjectiveFunction[Model]]
    
    val queenPath = config.getString("hivemind.queen.path");
    
    return new MonitorConfig(
        config.getInt("hivemind.monitor.batchSize"),
        objective)
  }
}

case class MonitorConfig(
    batchSize: Int, 
    objective: BatchObjectiveFunction[Model])