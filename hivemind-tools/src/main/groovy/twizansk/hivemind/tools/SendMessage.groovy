package twizansk.hivemind.tools

import twizansk.hivemind.messages.external.MsgConnectAndStart
import twizansk.hivemind.messages.external.MsgStop
import akka.actor.ActorSelection
import akka.actor.ActorSystem

class SendMessage {

    static main(args) {
		def cli = new CliBuilder(usage: 'SendMessage -[h] [p] [m] [q|d]')
		cli.with {
			h longOpt: 'host', args: 1, 'Host'
			p longOpt: 'port', args: 1, 'Port'
			s longOpt: 'start', 'Start the actor'
			t longOpt: 'stop', 'Stop the actor'
			d longOpt: 'drone', 'Target is a drone'
			q longOpt: 'queen', 'Target is the queen'
		}
		
		def options = cli.parse(args)
		
		if ((!options.d && !options.q) || (options.d && options.q)) {
			throw new IllegalArgumentException("Exactly one of -q or -d must be specified.")
		}
		
		if (!(options.s || options.t) || (options.s && options.t)) {
			throw new IllegalArgumentException("Exactly one of -s must be specified.")
		}
		
		def sys = options.q ? 'QueenSystem' : 'DroneSystem'
		def actor = options.q ? 'queen' : 'drone'
		def host = !options.h ? '127.0.0.1' : options.h
		def port = options.p
		def path = "akka.tcp://${sys}@${host}:${port}/user/${actor}"
		def message = {opts ->
			switch(opts) {
				case {opts.s}: return MsgConnectAndStart.instance()
				case {opts.t}: return MsgStop.instance()
			}
		} 
		def msg = message(options)
		
		println "Sending ${msg.getClass().getSimpleName()} to ${actor} at ${path}"		
		def system = ActorSystem.create("MySystem")
		ActorSelection selection =  system.actorSelection(path);
		selection.tell(msg, null);
		Thread.sleep(500)
		system.shutdown()
    }
}
