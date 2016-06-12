object Inc {
	def main(args: Array[String]):Unit = 
	{
		var i:Int=0

		for(j <- (1 to 1000000000))
		{
			i = inc(i)
		}

		println(i);
	}

	def inc(i:Int):Int=i+1
}
