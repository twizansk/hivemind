package twizansk.hivemind.integration.model

def data = new File("/home/twizansk/work/hivemind/hivemind-integration/src/main/resources/linear-regression.csv")
	.text
	.readLines()
	.collect {it.split(',\\s*').collect {it as Double}}
	
def sx = data.sum {it[0]}
def ssqx = data.sum {it[0]**2}
def sy = data.sum {it[1]}
def sxy = data.sum {it[0]*it[1]}
def n = data.size()

def b = (sxy - sy * ssqx / sx) / (sx - n*ssqx / sx)
def m = (sy - n*b) / sx

println b
println m

