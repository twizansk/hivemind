package twizansk.hivemind.monitor

import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ArrayBuffer
import akka.actor.Actor
import akka.actor.FSM
import akka.actor.Props
import twizansk.hivemind.api.data.TrainingSample
import twizansk.hivemind.api.data._
import twizansk.hivemind.messages.drone.MsgTrainingSample
import twizansk.hivemind.messages.external.MsgConnectAndStart
import twizansk.hivemind.messages.external.MsgStop
import java.util.Arrays

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

class Monitor(config: MonitorConfig) extends Actor with FSM[State, MonitorState] {

  startWith(Active, MonitorState(None, ArrayBuffer[TrainingSample]()))

  override def preStart(): Unit = {
	// TODO:  init queen	  
  }

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
      val J = config.objective.objective(Arrays.asList(state.samples: _*), null)
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