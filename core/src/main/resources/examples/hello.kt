fun main(args: Array<String>) {

	var x=1000000

	var sum=0

	while (x > 0)
	{
		x--
		sum = inc(sum)
	}			

	println("sum " + sum)
}

fun inc(x: Int): Int
{
	return x+1
}
