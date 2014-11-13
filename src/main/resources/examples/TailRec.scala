import scala.annotation.tailrec

object TailRec {
	def main(args: Array[String]):Unit = 
	{
		var i = inc(0, 1000000000)
	
		println(i);
	}

	@tailrec
	def inc(i:Int, iter:Int):
		Int=if (iter>0)
			inc(i+1, iter-1)
		else i
}
