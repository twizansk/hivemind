package twizansk.hivemind.monitor

import java.util.Arrays
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ArrayBuffer
import akka.actor.Actor
import akka.actor.FSM
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import twizansk.hivemind.api.data._
import twizansk.hivemind.api.data.TrainingSample
import twizansk.hivemind.common.RemoteActor
import twizansk.hivemind.messages.drone.MsgGetModel
import twizansk.hivemind.messages.drone.MsgTrainingSample
import twizansk.hivemind.messages.external.MsgConnectAndStart
import twizansk.hivemind.messages.external.MsgStop
import scala.concurrent.Await
import twizansk.hivemind.api.model.Model
import twizansk.hivemind.messages.queen.MsgModel

// received events
case object MsgSnapshot

// data
case class Stats(J: Double)
case class MonitorState(stats: Option[Stats], samples: ArrayBuffer[TrainingSample])

// states
sealed trait State
case object Idle extends State
case object Active extends State

object Monitor {

  def props(config: MonitorConfig): Props = Props(classOf[Monitor], config)

}

class Monitor(config: MonitorConfig) extends Actor with HivemindFSM[State, MonitorState] {
  
  val queen = registerRemoteActor(config.queenPath)
  
  override def preStart(): Unit = {
	queen.lookup()  
  }
  
  startWith(Active, MonitorState(None, ArrayBuffer[TrainingSample]()))

  when(Idle)({
    case Event(m: MsgConnectAndStart, _) ⇒ {
      goto(Active)
    }
  })

  when(Active)({
    // Add an incoming sample to the list of samples.
    case Event(m: MsgTrainingSample, state: MonitorState) if state.samples.size < config.batchSize => {
      state.samples += m.trainingSample
      stay
    }

    // If the sample list exceeds the snapshot threshold, create a stats snapshot
    case Event(m: MsgTrainingSample, state: MonitorState) => {
      implicit val timeout = Timeout(5 seconds)
      val modelFuture = queen.ref() ? MsgGetModel.instance
      val model = Await.result(modelFuture, timeout.duration).asInstanceOf[MsgModel].model
      val J = config.objective.objective(Arrays.asList(state.samples: _*), model)
      state.samples.clear()
      stay using state.copy(stats = Some(Stats(J)))
    }

    // Stop the monitor
    case Event(m: MsgStop, state: MonitorState) => {
      goto(Idle) using MonitorState(None, ArrayBuffer[TrainingSample]())
    }
  })

  initialize()
}