object IncWhile {
	def main(args: Array[String]):Unit = 
	{
		var i:Int=0
		var limit = 0;

		while(limit <  1000000000)
		{
			i = inc(i)
			limit = limit + 1;
		}

		println(i);
	}

	def inc(i:Int):Int=i+1
}
