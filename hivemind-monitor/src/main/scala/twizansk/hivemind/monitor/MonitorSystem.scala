package twizansk.hivemind.monitor

import akka.actor.ActorSystem
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

object MonitorSystem {

  def init(config: MonitorConfig) {
    val system = ActorSystem("MonitorSystem")
    val monitor = system.actorOf(Monitor.props(config), "monitor")
  }
  
  def main(args: Array[String]) {
    var confFile = System.getProperty("hivemind.conf");
	if (confFile == null) {
		confFile = "hivemind"; 
	}
	val config = ConfigFactory.load(confFile);
	val monitorConfig = MonitorConfig.createConfig(config);
    
    init(monitorConfig)
  }

}