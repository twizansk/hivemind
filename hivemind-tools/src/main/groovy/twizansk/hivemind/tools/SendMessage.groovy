package twizansk.hivemind.tools

import org.apache.commons.cli.BasicParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionGroup
import org.apache.commons.cli.Options

import twizansk.hivemind.messages.external.MsgConnectAndStart
import twizansk.hivemind.messages.external.MsgStop
import akka.actor.ActorSelection
import akka.actor.ActorSystem

class SendMessage {

    static main(args) {
		// create Options object
		Options options = new Options();
		
		options.addOption("h", false, "Host")
		options.addOption("p", true, "Port")
		
		OptionGroup commandGroup = new OptionGroup()
		commandGroup.addOption(new Option("s", false, "Start"))
		commandGroup.addOption(new Option("t", false, "Stop"))
		options.addOptionGroup(commandGroup)
		
		OptionGroup actorGroup = new OptionGroup()
		actorGroup.addOption(new Option("q", false, "Queen"))
		actorGroup.addOption(new Option("d", false, "Drone"))
		actorGroup.addOption(new Option("m", false, "Monitor"))
		options.addOptionGroup(actorGroup)
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);
		
		def host = !cmd.hasOption("h") ? '127.0.0.1' : cmd.getOptionValue("h")
		def port = cmd.getOptionValue("p")
		def actorSystem = {c ->
			switch(c) {
				case {c.hasOption("q")}: return ['queen', 'QueenSystem']
				case {c.hasOption("d")}: return ['drone', 'DroneSystem']
				case {c.hasOption("m")}: return ['monitor', 'MonitorSystem']
			}
		} 
		
		def message = {c ->
			switch(c) {
				case {c.hasOption("s")}: return MsgConnectAndStart.instance()
				case {c.hasOption("t")}: return MsgStop.instance()
			}
		} 
		def msg = message(cmd)
		def (actor, sys) = actorSystem(cmd)
		
		def path = "akka.tcp://${sys}@${host}:${port}/user/${actor}"
		println "Sending ${msg.getClass().getSimpleName()} to ${actor} at ${path}"		
		def system = ActorSystem.create("MySystem")
		ActorSelection selection =  system.actorSelection(path);
		selection.tell(msg, null);
		Thread.sleep(500)
		system.shutdown()
    }
}
